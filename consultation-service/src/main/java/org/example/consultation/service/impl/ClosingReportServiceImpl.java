package org.example.consultation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.xwpf.usermodel.*;
import org.example.common.exception.BusinessException;
import org.example.consultation.dto.ClosingReportQueryDTO;
import org.example.consultation.dto.ClosingReportReviewDTO;
import org.example.consultation.dto.ClosingReportSaveDTO;
import org.example.consultation.entity.ClosingReport;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.mapper.ClosingReportMapper;
import org.example.consultation.mapper.ConsultationAppointmentMapper;
import org.example.consultation.service.ClosingReportService;
import org.example.common.support.StatisticsSyncSupport;
import org.example.common.support.UserLookupSupport;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 结案报告 Service 实现
 */
@Service
public class ClosingReportServiceImpl extends ServiceImpl<ClosingReportMapper, ClosingReport>
        implements ClosingReportService {

    private static final String REPORT_DIR = "./reports/";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 问题类型名称映射 */
    private static final String[] PROBLEM_TYPE_NAMES = {
            "", "学业问题", "情绪问题", "人际关系", "恋爱问题", "职业发展", "自我成长", "家庭问题", "其他"
    };

    private final StatisticsSyncSupport statisticsSyncSupport;
    private final ConsultationAppointmentMapper consultationAppointmentMapper;
    private final ClosingReportMapper closingReportMapper;
    private final UserLookupSupport userLookupSupport;

    public ClosingReportServiceImpl(StatisticsSyncSupport statisticsSyncSupport,
                                    ConsultationAppointmentMapper consultationAppointmentMapper,
                                    ClosingReportMapper closingReportMapper,
                                    UserLookupSupport userLookupSupport) {
        this.statisticsSyncSupport = statisticsSyncSupport;
        this.consultationAppointmentMapper = consultationAppointmentMapper;
        this.closingReportMapper = closingReportMapper;
        this.userLookupSupport = userLookupSupport;
    }

    // ==================== 查询 ====================

    @Override
    public Page<ClosingReport> pageQuery(ClosingReportQueryDTO queryDTO) {
        LambdaQueryWrapper<ClosingReport> wrapper = buildQueryWrapper(queryDTO);
        Page<ClosingReport> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return this.page(page, wrapper);
    }

    @Override
    public ClosingReport getDetailById(Long id) {
        ClosingReport report = this.getById(id);
        if (report == null) {
            throw new BusinessException(404, "结案报告不存在");
        }
        return report;
    }

    @Override
    public List<ClosingReport> query(String studentNo, String studentName, Long counselorId, Integer problemType) {
        LambdaQueryWrapper<ClosingReport> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(studentNo)) wrapper.eq(ClosingReport::getStudentNo, studentNo);
        if (StringUtils.hasText(studentName)) wrapper.like(ClosingReport::getStudentName, studentName);
        if (counselorId != null) wrapper.eq(ClosingReport::getCounselorId, counselorId);
        if (problemType != null) wrapper.eq(ClosingReport::getProblemType, problemType);
        wrapper.orderByDesc(ClosingReport::getCreateTime);
        return list(wrapper);
    }

    // ==================== 新增 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClosingReport create(Long counselorId, ClosingReportSaveDTO saveDTO) {
        ClosingReport report = new ClosingReport();
        BeanUtil.copyProperties(saveDTO, report);
        report.setCounselorId(counselorId);

        // 设置默认值
        setDefaults(report);

        if (!this.save(report)) {
            throw new BusinessException("新增结案报告失败");
        }
        syncIfSubmitted(this.getById(report.getId()));
        return this.getById(report.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClosingReport submit(Long counselorId, ClosingReport report) {
        ClosingReport target;
        if (report.getId() != null) {
            target = getDetailById(report.getId());
            if (target.getCounselorId() != null && !target.getCounselorId().equals(counselorId)) {
                throw new BusinessException("无权提交此报告");
            }
            if (!"草稿".equals(target.getStatus())) {
                throw new BusinessException("仅草稿状态可提交");
            }
            BeanUtil.copyProperties(report, target, CopyOptions.create().ignoreNullValue());
        } else {
            target = report;
            target.setCounselorId(counselorId);
            setDefaults(target);
        }
        target.setCounselorId(counselorId);
        target.setStatus("已提交");

        if (target.getId() == null) {
            save(target);
        } else {
            updateById(target);
        }

        String path = generateWord(target.getId());
        target.setFilePath(path);
        updateById(target);

        syncIfSubmitted(getById(target.getId()));
        return getById(target.getId());
    }

    // ==================== 修改 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClosingReport update(Long id, ClosingReportSaveDTO saveDTO) {
        ClosingReport existing = this.getDetailById(id);

        ClosingReport report = new ClosingReport();
        BeanUtil.copyProperties(saveDTO, report);
        report.setId(id);

        if (saveDTO.getAppointmentId() != null) {
            validateClosableAppointment(saveDTO.getAppointmentId(), existing.getCounselorId(), id);
        }

        if (!this.updateById(report)) {
            throw new BusinessException("修改结案报告失败");
        }
        ClosingReport updated = this.getById(id);
        syncIfSubmitted(updated);
        return updated;
    }

    // ==================== 删除 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 先查询确认存在
        this.getDetailById(id);
        if (!this.removeById(id)) {
            throw new BusinessException("删除结案报告失败");
        }
    }

    // ==================== Word 生成 ====================

    @Override
    public String generateWord(Long reportId) {
        ClosingReport report = getById(reportId);
        if (report == null) throw new BusinessException("报告不存在");

        try {
            File dir = new File(REPORT_DIR);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "结案报告_" + report.getStudentNo() + "_" + report.getStudentName() + ".docx";
            String filePath = REPORT_DIR + fileName;

            XWPFDocument doc = new XWPFDocument();

            // ===== 标题 =====
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            title.setSpacingAfter(200);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("心理咨询结案报告");
            titleRun.setBold(true);
            titleRun.setFontSize(20);
            titleRun.setFontFamily("SimHei");

            // ===== 空行 =====
            XWPFParagraph blank = doc.createParagraph();
            blank.setSpacingAfter(100);

            // ===== 一、学生基本信息 =====
            addSectionHeading(doc, "一、学生基本信息");
            XWPFTable table1 = createStyledTable(doc);
            addTableRow(table1, 0, "学号", safeStr(report.getStudentNo()));
            addTableRow(table1, 1, "姓名", safeStr(report.getStudentName()));
            addTableRow(table1, 2, "性别", safeStr(report.getGender()));
            addTableRow(table1, 3, "年级", safeStr(report.getStudentGrade()));
            addTableRow(table1, 4, "院系", safeStr(report.getDepartment()));
            addTableRow(table1, 5, "专业", safeStr(report.getStudentMajor()));
            addTableRow(table1, 6, "联系电话", safeStr(report.getPhone()));
            addTableRow(table1, 7, "电子邮箱", safeStr(report.getStudentEmail()));
            doc.createParagraph().setSpacingAfter(100);

            // ===== 二、咨询基本信息 =====
            addSectionHeading(doc, "二、咨询基本信息");
            XWPFTable table2 = createStyledTable(doc);
            addTableRow(table2, 0, "咨询安排ID", nvlStr(report.getAppointmentId()));
            addTableRow(table2, 1, "咨询师ID", nvlStr(report.getCounselorId()));
            addTableRow(table2, 2, "问题类型", getProblemTypeName(report.getProblemType()));
            addTableRow(table2, 3, "咨询方式", safeStr(report.getConsultationMethod()));
            addTableRow(table2, 4, "首次咨询日期", formatDateTime(report.getFirstConsultationDate()));
            addTableRow(table2, 5, "结案日期", formatDateTime(report.getClosingDate()));
            addTableRow(table2, 6, "咨询总次数", nvlStr(report.getTotalSessions()));
            addTableRow(table2, 7, "总咨询时长（小时）", nvlDecimal(report.getTotalHours()));
            doc.createParagraph().setSpacingAfter(100);

            // ===== 三、结案核心内容 =====
            addSectionHeading(doc, "三、结案核心内容");
            XWPFTable table3 = createStyledTable(doc);
            addTableRow(table3, 0, "结案原因", safeStr(report.getClosingReason()));
            addTableRow(table3, 1, "结案原因详细说明", safeStr(report.getClosingReasonDetail()));
            addTableRow(table3, 2, "个案摘要", safeStr(report.getCaseSummary()));
            addTableRow(table3, 3, "咨询效果自评（来访者）", safeStr(report.getSelfEvaluation()));
            addTableRow(table3, 4, "咨询效果评估（咨询师）", safeStr(report.getCounselingOutcome()));
            addTableRow(table3, 5, "后续跟进计划", safeStr(report.getFollowUpPlan()));
            addTableRow(table3, 6, "转介信息", safeStr(report.getReferralInfo()));
            doc.createParagraph().setSpacingAfter(100);

            // ===== 四、风险评估与审核 =====
            addSectionHeading(doc, "四、风险评估与审核");
            XWPFTable table4 = createStyledTable(doc);
            addTableRow(table4, 0, "风险评估等级", safeStr(report.getRiskLevel()));
            addTableRow(table4, 1, "风险备注", safeStr(report.getRiskNote()));
            addTableRow(table4, 2, "报告状态", safeStr(report.getStatus()));
            addTableRow(table4, 3, "审核人", safeStr(report.getReviewerName()));
            addTableRow(table4, 4, "审核意见", safeStr(report.getReviewComment()));
            addTableRow(table4, 5, "审核日期", formatDateTime(report.getReviewDate()));
            doc.createParagraph().setSpacingAfter(200);

            // ===== 页脚日期 =====
            XWPFParagraph footer = doc.createParagraph();
            footer.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun footerRun = footer.createRun();
            footerRun.setText("生成日期：" + LocalDateTime.now().format(DATE_FMT));
            footerRun.setFontSize(10);
            footerRun.setFontFamily("SimSun");
            footerRun.setColor("888888");

            // ===== 写入文件 =====
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                doc.write(out);
            }
            doc.close();

            ClosingReport pathUpdate = new ClosingReport();
            pathUpdate.setId(reportId);
            pathUpdate.setFilePath(filePath);
            updateById(pathUpdate);

            return filePath;
        } catch (Exception e) {
            throw new BusinessException("Word 生成失败: " + e.getMessage());
        }
    }

    // ==================== Word 下载 ====================

    @Override
    public void downloadWord(Long reportId, jakarta.servlet.http.HttpServletResponse response) {
        ClosingReport report = getDetailById(reportId);
        String filePath = report.getFilePath();
        if (!StringUtils.hasText(filePath)) {
            throw new BusinessException(404, "该报告尚未生成Word文件，请先生成");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            throw new BusinessException(404, "Word文件不存在，请重新生成");
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setCharacterEncoding("UTF-8");
        String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename*=UTF-8''" + encodedFileName);

        try (OutputStream out = response.getOutputStream();
             FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            throw new BusinessException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public int resyncAllToStatistics() {
        List<ClosingReport> reports = list(new LambdaQueryWrapper<ClosingReport>()
                .ne(ClosingReport::getStatus, "草稿"));
        for (ClosingReport report : reports) {
            syncIfSubmitted(report);
        }
        return reports.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClosingReport review(Long reviewerId, Long id, ClosingReportReviewDTO dto) {
        ClosingReport report = getDetailById(id);
        if (!"已提交".equals(report.getStatus())) {
            throw new BusinessException("仅已提交的报告可审核");
        }
        if (!"已审核".equals(dto.getStatus()) && !"已驳回".equals(dto.getStatus())) {
            throw new BusinessException("审核状态无效");
        }
        report.setStatus(dto.getStatus());
        report.setReviewComment(dto.getReviewComment());
        report.setReviewerId(reviewerId);
        report.setReviewerName(userLookupSupport.getDisplayName(reviewerId));
        report.setReviewDate(LocalDateTime.now());
        updateById(report);
        syncIfSubmitted(getById(id));
        return getById(id);
    }

    // ==================== 私有方法 ====================

    /**
     * 构建分页查询条件
     */
    private LambdaQueryWrapper<ClosingReport> buildQueryWrapper(ClosingReportQueryDTO queryDTO) {
        LambdaQueryWrapper<ClosingReport> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(queryDTO.getAppointmentId() != null,
                        ClosingReport::getAppointmentId, queryDTO.getAppointmentId())
                .like(StringUtils.hasText(queryDTO.getStudentNo()),
                        ClosingReport::getStudentNo, queryDTO.getStudentNo())
                .like(StringUtils.hasText(queryDTO.getStudentName()),
                        ClosingReport::getStudentName, queryDTO.getStudentName())
                .eq(StringUtils.hasText(queryDTO.getGender()),
                        ClosingReport::getGender, queryDTO.getGender())
                .like(StringUtils.hasText(queryDTO.getStudentGrade()),
                        ClosingReport::getStudentGrade, queryDTO.getStudentGrade())
                .eq(queryDTO.getCounselorId() != null,
                        ClosingReport::getCounselorId, queryDTO.getCounselorId())
                .eq(queryDTO.getProblemType() != null,
                        ClosingReport::getProblemType, queryDTO.getProblemType())
                .eq(StringUtils.hasText(queryDTO.getClosingReason()),
                        ClosingReport::getClosingReason, queryDTO.getClosingReason())
                .eq(StringUtils.hasText(queryDTO.getConsultationMethod()),
                        ClosingReport::getConsultationMethod, queryDTO.getConsultationMethod())
                .eq(StringUtils.hasText(queryDTO.getStatus()),
                        ClosingReport::getStatus, queryDTO.getStatus())
                .eq(StringUtils.hasText(queryDTO.getRiskLevel()),
                        ClosingReport::getRiskLevel, queryDTO.getRiskLevel())
                .eq(StringUtils.hasText(queryDTO.getDepartment()),
                        ClosingReport::getDepartment, queryDTO.getDepartment())
                .ge(queryDTO.getFirstConsultationStart() != null,
                        ClosingReport::getFirstConsultationDate, queryDTO.getFirstConsultationStart())
                .le(queryDTO.getFirstConsultationEnd() != null,
                        ClosingReport::getFirstConsultationDate, queryDTO.getFirstConsultationEnd())
                .ge(queryDTO.getClosingDateStart() != null,
                        ClosingReport::getClosingDate, queryDTO.getClosingDateStart())
                .le(queryDTO.getClosingDateEnd() != null,
                        ClosingReport::getClosingDate, queryDTO.getClosingDateEnd());

        // 默认按创建时间降序
        wrapper.orderByDesc(ClosingReport::getCreateTime);

        return wrapper;
    }

    // ---- Word 生成辅助方法 ----

    /** 创建带边框样式的空表格 */
    private XWPFTable createStyledTable(XWPFDocument doc) {
        XWPFTable table = doc.createTable();
        table.setWidth("100%");
        // 设置表格边框
        CTTbl ctTbl = table.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr() != null ? ctTbl.getTblPr() : ctTbl.addNewTblPr();
        CTTblBorders borders = tblPr.addNewTblBorders();
        setBorder(borders.addNewTop());
        setBorder(borders.addNewBottom());
        setBorder(borders.addNewLeft());
        setBorder(borders.addNewRight());
        setBorder(borders.addNewInsideH());
        setBorder(borders.addNewInsideV());
        return table;
    }

    private void setBorder(CTBorder border) {
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setColor("000000");
    }

    private XWPFTableRow addTableRow(XWPFTable table, int rowIdx, String label, String value) {
        XWPFTableRow row = rowIdx < table.getNumberOfRows() ? table.getRow(rowIdx) : table.createRow();
        // 确保两个单元格存在（createTable 默认只有 1 列）
        XWPFTableCell cellLabel = row.getCell(0) != null ? row.getCell(0) : row.createCell();
        XWPFTableCell cellValue = row.getCell(1) != null ? row.getCell(1) : row.createCell();

        // 标签单元格样式
        setCellText(cellLabel, label, true);

        // 值单元格样式
        setCellText(cellValue, value, false);

        return row;
    }

    private void setCellText(XWPFTableCell cell, String text, boolean isLabel) {
        // 清除默认段落
        cell.removeParagraph(0);
        XWPFParagraph para = cell.addParagraph();
        para.setAlignment(ParagraphAlignment.LEFT);
        // 垂直居中
        cell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);

        // 设置单元格宽度
        CTTc ctTc = cell.getCTTc();
        CTTcPr tcPr = ctTc.getTcPr() != null ? ctTc.getTcPr() : ctTc.addNewTcPr();
        CTTblWidth width = tcPr.getTcW() != null ? tcPr.getTcW() : tcPr.addNewTcW();
        width.setW(isLabel ? java.math.BigInteger.valueOf(2500) : java.math.BigInteger.valueOf(7000));
        width.setType(STTblWidth.DXA);

        XWPFRun run = para.createRun();
        run.setText(text != null ? text : "");
        run.setFontFamily("SimSun");
        run.setFontSize(11);
        if (isLabel) {
            run.setBold(true);
        }
    }

    private void addSectionHeading(XWPFDocument doc, String text) {
        XWPFParagraph para = doc.createParagraph();
        para.setSpacingBefore(200);
        para.setSpacingAfter(100);
        XWPFRun run = para.createRun();
        run.setText(text);
        run.setBold(true);
        run.setFontSize(14);
        run.setFontFamily("SimHei");
        // 下划线
        run.setUnderline(UnderlinePatterns.SINGLE);
    }

    /** 设置默认值 */
    private void setDefaults(ClosingReport report) {
        if (!StringUtils.hasText(report.getConsultationMethod())) {
            report.setConsultationMethod("面对面");
        }
        if (!StringUtils.hasText(report.getStatus())) {
            report.setStatus("草稿");
        }
        if (!StringUtils.hasText(report.getRiskLevel())) {
            report.setRiskLevel("低");
        }
        if (!StringUtils.hasText(report.getSelfEvaluation())) {
            report.setSelfEvaluation("暂无");
        }
        if (!StringUtils.hasText(report.getGender())) {
            report.setGender("男");
        }
        if (!StringUtils.hasText(report.getDepartment())) {
            report.setDepartment("未填写");
        }
        if (!StringUtils.hasText(report.getPhone())) {
            report.setPhone("00000000000");
        }
        if (report.getAppointmentId() == null) {
            throw new BusinessException("请填写咨询安排ID");
        }
        validateClosableAppointment(report.getAppointmentId(), report.getCounselorId(), null);
        if (report.getClosingDate() == null) {
            throw new BusinessException("请选择结案日期");
        }
        if (!StringUtils.hasText(report.getClosingReason())) {
            throw new BusinessException("请选择结案原因");
        }
        if (report.getTotalSessions() == null) {
            report.setTotalSessions(0);
        }
        if (report.getTotalHours() == null) {
            report.setTotalHours(BigDecimal.ZERO);
        }
    }

    /** 非草稿状态同步至统计服务（提交/已审核等） */
    private void syncIfSubmitted(ClosingReport report) {
        if (report == null || !StringUtils.hasText(report.getStatus()) || "草稿".equals(report.getStatus())) {
            return;
        }
        statisticsSyncSupport.syncClosingReport(report);
    }

    private void validateClosableAppointment(Long appointmentId, Long counselorId, Long excludeReportId) {
        ConsultationAppointment appointment = consultationAppointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("关联的咨询安排不存在");
        }
        if (counselorId != null && appointment.getCounselorId() != null
                && !appointment.getCounselorId().equals(counselorId)) {
            throw new BusinessException("只能关联您负责的咨询安排");
        }
        if (appointment.getStatus() == null || (appointment.getStatus() != 2 && appointment.getStatus() != 3)) {
            throw new BusinessException("只能关联已结束（结案/脱落）的咨询安排，请先在咨询记录中完成结案");
        }
        LambdaQueryWrapper<ClosingReport> wrapper = new LambdaQueryWrapper<ClosingReport>()
                .eq(ClosingReport::getAppointmentId, appointmentId);
        if (excludeReportId != null) {
            wrapper.ne(ClosingReport::getId, excludeReportId);
        }
        if (closingReportMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该咨询安排已有关联的结案报告");
        }
    }

    // ---- 通用辅助方法 ----

    private String safeStr(String s) {
        return StringUtils.hasText(s) ? s : "";
    }

    private String nvlStr(Object val) {
        return val != null ? val.toString() : "";
    }

    private String nvlDecimal(BigDecimal d) {
        return d != null ? d.setScale(2, RoundingMode.HALF_UP).toString() : "0.00";
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt != null ? dt.format(DATE_FMT) : "";
    }

    private String getProblemTypeName(Integer code) {
        return code != null && code > 0 && code < PROBLEM_TYPE_NAMES.length ? PROBLEM_TYPE_NAMES[code] : "未知";
    }
}
