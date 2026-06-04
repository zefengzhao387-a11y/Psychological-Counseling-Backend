package org.example.consultation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.xwpf.usermodel.*;
import org.example.common.exception.BusinessException;
import org.example.consultation.dto.ClosingReportQueryDTO;
import org.example.consultation.dto.ClosingReportSaveDTO;
import org.example.consultation.entity.ClosingReport;
import org.example.consultation.mapper.ClosingReportMapper;
import org.example.consultation.service.ClosingReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结案报告 Service 实现
 */
@Service
public class ClosingReportServiceImpl extends ServiceImpl<ClosingReportMapper, ClosingReport>
        implements ClosingReportService {

    private static final String REPORT_DIR = "./reports/";

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
    public ClosingReport create(ClosingReportSaveDTO saveDTO) {
        ClosingReport report = new ClosingReport();
        BeanUtil.copyProperties(saveDTO, report);

        // 设置默认值
        setDefaults(report);

        if (!this.save(report)) {
            throw new BusinessException("新增结案报告失败");
        }
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClosingReport submit(Long counselorId, ClosingReport report) {
        report.setCounselorId(counselorId);
        if (!StringUtils.hasText(report.getStatus())) {
            report.setStatus("已提交");
        }
        save(report);

        // 生成 Word
        String path = generateWord(report.getId());
        report.setFilePath(path);
        updateById(report);

        return report;
    }

    // ==================== 修改 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClosingReport update(Long id, ClosingReportSaveDTO saveDTO) {
        // 先查询确认存在
        ClosingReport existing = this.getDetailById(id);

        ClosingReport report = new ClosingReport();
        BeanUtil.copyProperties(saveDTO, report);
        report.setId(id);

        if (!this.updateById(report)) {
            throw new BusinessException("修改结案报告失败");
        }
        return this.getById(id);
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

            // 标题
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = title.createRun();
            run.setText("心理咨询结案报告");
            run.setBold(true);
            run.setFontSize(16);

            // 基本信息表格
            String[][] fields = {
                    {"来访者学号", report.getStudentNo()},
                    {"来访者姓名", report.getStudentName()},
                    {"来访者性别", report.getGender()},
                    {"来访者院系", report.getDepartment()},
                    {"来访者联系电话", report.getPhone()},
                    {"问题类型", getProblemTypeName(report.getProblemType())},
                    {"咨询方式", report.getConsultationMethod() != null ? report.getConsultationMethod() : ""},
                    {"咨询总次数", String.valueOf(report.getTotalSessions() != null ? report.getTotalSessions() : 0)},
                    {"总咨询时长(小时)", report.getTotalHours() != null ? report.getTotalHours().toString() : "0"},
                    {"结案原因", report.getClosingReason() != null ? report.getClosingReason() : ""},
                    {"咨询效果自评", report.getSelfEvaluation() != null ? report.getSelfEvaluation() : ""},
                    {"填表日期", java.time.LocalDate.now().toString()},
            };

            XWPFTable table = doc.createTable(fields.length, 2);
            table.setWidth("100%");

            for (int i = 0; i < fields.length; i++) {
                XWPFTableRow row = table.getRow(i);
                row.getCell(0).setText(fields[i][0]);
                row.getCell(1).setText(fields[i][1]);
            }

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                doc.write(out);
            }
            doc.close();
            return filePath;
        } catch (Exception e) {
            throw new BusinessException("Word 生成失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建分页查询条件
     */
    private LambdaQueryWrapper<ClosingReport> buildQueryWrapper(ClosingReportQueryDTO queryDTO) {
        LambdaQueryWrapper<ClosingReport> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(queryDTO.getStudentNo()),
                        ClosingReport::getStudentNo, queryDTO.getStudentNo())
                .like(StringUtils.hasText(queryDTO.getStudentName()),
                        ClosingReport::getStudentName, queryDTO.getStudentName())
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

    /**
     * 设置默认值
     */
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
        if (report.getTotalSessions() == null) {
            report.setTotalSessions(0);
        }
        if (report.getTotalHours() == null) {
            report.setTotalHours(BigDecimal.ZERO);
        }
    }

    /**
     * 根据问题类型编码获取名称
     */
    private String getProblemTypeName(Integer code) {
        String[] names = {"", "学业问题", "情绪问题", "人际关系", "恋爱问题", "职业发展", "自我成长", "家庭问题", "其他"};
        return code != null && code > 0 && code < names.length ? names[code] : String.valueOf(code);
    }
}
