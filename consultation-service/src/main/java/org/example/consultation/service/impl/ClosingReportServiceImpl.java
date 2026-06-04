package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.xwpf.usermodel.*;
import org.example.common.exception.BusinessException;
import org.example.consultation.entity.ClosingReport;
import org.example.consultation.mapper.ClosingReportMapper;
import org.example.consultation.service.ClosingReportService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@Service
public class ClosingReportServiceImpl extends ServiceImpl<ClosingReportMapper, ClosingReport>
        implements ClosingReportService {

    private static final String REPORT_DIR = "./reports/";

    @Override
    public ClosingReport submit(Long counselorId, ClosingReport report) {
        report.setCounselorId(counselorId);
        save(report);

        // 生成 Word
        String path = generateWord(report.getId());
        report.setFilePath(path);
        updateById(report);

        return report;
    }

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
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = title.createRun();
            run.setText("心理咨询结案报告");
            run.setBold(true);
            run.setFontSize(16);

            // 基本信息表格
            XWPFTable table = doc.createTable(9, 2);
            table.setWidth("100%");

            String[][] fields = {
                    {"来访者学号", report.getStudentNo()},
                    {"来访者姓名", report.getStudentName()},
                    {"来访者性别", report.getGender()},
                    {"来访者院系", report.getDepartment()},
                    {"来访者联系电话", report.getPhone()},
                    {"问题类型", getProblemTypeName(report.getProblemType())},
                    {"咨询总次数", String.valueOf(report.getTotalSessions())},
                    {"咨询效果自评", report.getSelfEvaluation()},
                    {"填表日期", java.time.LocalDate.now().toString()},
            };

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

    @Override
    public List<ClosingReport> query(String studentNo, String studentName, Long counselorId, Integer problemType) {
        LambdaQueryWrapper<ClosingReport> wrapper = new LambdaQueryWrapper<>();
        if (studentNo != null && !studentNo.isEmpty()) wrapper.eq(ClosingReport::getStudentNo, studentNo);
        if (studentName != null && !studentName.isEmpty()) wrapper.like(ClosingReport::getStudentName, studentName);
        if (counselorId != null) wrapper.eq(ClosingReport::getCounselorId, counselorId);
        if (problemType != null) wrapper.eq(ClosingReport::getProblemType, problemType);
        return list(wrapper);
    }

    private String getProblemTypeName(Integer code) {
        String[] names = {"", "学业问题", "情绪问题", "人际关系", "恋爱问题", "职业发展", "自我成长", "家庭问题", "其他"};
        return code != null && code < names.length ? names[code] : String.valueOf(code);
    }
}
