package org.example.statistics.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.example.statistics.dto.CounselorStatsDTO;
import org.example.statistics.entity.ClosingReport;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 导出工具类（基于 EasyExcel）
 */
public class ExcelExportUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] PROBLEM_TYPE_NAMES = {
            "", "学业问题", "情绪问题", "人际关系", "恋爱问题", "职业发展", "自我成长", "家庭问题", "其他"
    };

    /**
     * 导出结案报告 Excel
     */
    public static void exportClosingReport(OutputStream outputStream, List<ClosingReport> records) {
        List<Map<Integer, String>> data = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            ClosingReport r = records.get(i);
            Map<Integer, String> row = new LinkedHashMap<>();
            int c = 0;
            row.put(c++, String.valueOf(i + 1));
            row.put(c++, nvl(r.getAppointmentId()));
            row.put(c++, nvl(r.getCounselorId()));
            row.put(c++, nvl(r.getStudentNo()));
            row.put(c++, nvl(r.getStudentName()));
            row.put(c++, nvl(r.getGender()));
            row.put(c++, nvl(r.getStudentGrade()));
            row.put(c++, nvl(r.getDepartment()));
            row.put(c++, nvl(r.getStudentMajor()));
            row.put(c++, nvl(r.getPhone()));
            row.put(c++, nvl(r.getStudentEmail()));
            row.put(c++, getProblemTypeName(r.getProblemType()));
            row.put(c++, nvl(r.getConsultationMethod()));
            row.put(c++, formatDate(r.getFirstConsultationDate()));
            row.put(c++, formatDate(r.getClosingDate()));
            row.put(c++, nvl(r.getTotalSessions()));
            row.put(c++, nvl(r.getTotalHours()));
            row.put(c++, nvl(r.getClosingReason()));
            row.put(c++, nvl(r.getClosingReasonDetail()));
            row.put(c++, nvl(r.getCaseSummary()));
            row.put(c++, nvl(r.getSelfEvaluation()));
            row.put(c++, nvl(r.getCounselingOutcome()));
            row.put(c++, nvl(r.getFollowUpPlan()));
            row.put(c++, nvl(r.getRiskLevel()));
            row.put(c++, nvl(r.getRiskNote()));
            row.put(c++, nvl(r.getStatus()));
            row.put(c++, nvl(r.getReviewerName()));
            row.put(c++, formatDate(r.getReviewDate()));
            row.put(c++, formatDate(r.getCreateTime()));
            data.add(row);
        }

        EasyExcel.write(outputStream)
                .head(buildHead())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("结案报告")
                .doWrite(() -> data);
    }

    private static List<List<String>> buildHead() {
        List<List<String>> head = new ArrayList<>();
        head.add(singleHead("序号"));
        head.add(singleHead("咨询安排ID"));
        head.add(singleHead("咨询师ID"));
        head.add(singleHead("学号"));
        head.add(singleHead("学生姓名"));
        head.add(singleHead("性别"));
        head.add(singleHead("年级"));
        head.add(singleHead("院系"));
        head.add(singleHead("专业"));
        head.add(singleHead("电话"));
        head.add(singleHead("邮箱"));
        head.add(singleHead("问题类型"));
        head.add(singleHead("咨询方式"));
        head.add(singleHead("首次咨询日期"));
        head.add(singleHead("结案日期"));
        head.add(singleHead("总咨询次数"));
        head.add(singleHead("总咨询时长(h)"));
        head.add(singleHead("结案原因"));
        head.add(singleHead("结案原因说明"));
        head.add(singleHead("个案摘要"));
        head.add(singleHead("咨询效果自评"));
        head.add(singleHead("咨询效果评估"));
        head.add(singleHead("后续跟进计划"));
        head.add(singleHead("风险等级"));
        head.add(singleHead("风险备注"));
        head.add(singleHead("状态"));
        head.add(singleHead("审核人"));
        head.add(singleHead("审核日期"));
        head.add(singleHead("创建时间"));
        return head;
    }

    private static List<String> singleHead(String name) {
        List<String> list = new ArrayList<>();
        list.add(name);
        return list;
    }

    private static String nvl(Object val) {
        return val != null ? val.toString() : "";
    }

    private static String getProblemTypeName(Integer code) {
        return code != null && code > 0 && code < PROBLEM_TYPE_NAMES.length
                ? PROBLEM_TYPE_NAMES[code] : "";
    }

    private static String formatDate(LocalDateTime dt) {
        return dt != null ? dt.format(DATE_FMT) : "";
    }

    // ==================== 咨询师统计导出 ====================

    /**
     * 导出咨询师工作量统计到 Excel
     */
    public static void exportCounselorStats(OutputStream outputStream, List<CounselorStatsDTO> stats) {
        List<Map<Integer, String>> data = new ArrayList<>();

        for (int i = 0; i < stats.size(); i++) {
            CounselorStatsDTO s = stats.get(i);
            Map<Integer, String> row = new LinkedHashMap<>();
            int c = 0;
            row.put(c++, String.valueOf(i + 1));
            row.put(c++, nvl(s.getCounselorId()));
            row.put(c++, nvl(s.getTotalReports()));
            row.put(c++, nvl(s.getClosedCount()));
            row.put(c++, nvl(s.getDropoutCount()));
            row.put(c++, s.getTotalHours() != null ? s.getTotalHours().toString() : "0");
            row.put(c++, nvl(s.getProblemTypeBreakdown()));
            data.add(row);
        }

        EasyExcel.write(outputStream)
                .head(buildCounselorHead())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("咨询师工作量统计")
                .doWrite(() -> data);
    }

    private static List<List<String>> buildCounselorHead() {
        List<List<String>> head = new ArrayList<>();
        head.add(singleHead("序号"));
        head.add(singleHead("咨询师ID"));
        head.add(singleHead("报告总数"));
        head.add(singleHead("已结案数"));
        head.add(singleHead("脱落数"));
        head.add(singleHead("总咨询时长(h)"));
        head.add(singleHead("问题类型分布"));
        return head;
    }
}
