package org.example.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.statistics.dto.CounselorStatsDTO;
import org.example.statistics.dto.ProblemTypeStatsDTO;
import org.example.statistics.dto.StatisticsQueryDTO;
import org.example.statistics.dto.SummaryStatsDTO;
import org.example.statistics.entity.ClosingReport;
import org.example.statistics.mapper.ClosingReportMapper;
import org.example.statistics.service.StatisticsService;
import org.example.statistics.util.ExcelExportUtil;
import org.example.statistics.util.ZipDownloadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ClosingReportMapper closingReportMapper;

    @Value("${closing-report.files-dir:./reports/}")
    private String reportFilesDir;

    /** 问题类型名称映射 */
    private static final String[] PROBLEM_TYPE_NAMES = {
            "", "学业问题", "情绪问题", "人际关系", "恋爱问题", "职业发展", "自我成长", "家庭问题", "其他"
    };

    // ==================== 筛选查询 ====================

    @Override
    public Page<ClosingReport> filterQuery(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<ClosingReport> wrapper = buildQueryWrapper(queryDTO);
        Page<ClosingReport> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        return closingReportMapper.selectPage(page, wrapper);
    }

    // ==================== 综合汇总统计 ====================

    @Override
    public SummaryStatsDTO getSummaryStats(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<ClosingReport> wrapper = buildQueryWrapper(queryDTO);
        List<ClosingReport> allRecords = closingReportMapper.selectList(wrapper);

        SummaryStatsDTO summary = new SummaryStatsDTO();
        long total = allRecords.size();
        summary.setTotalReports(total);

        if (total == 0) {
            summary.setApprovedCount(0L);
            summary.setDraftCount(0L);
            summary.setRejectedCount(0L);
            summary.setTotalSessions(0L);
            summary.setTotalHours(BigDecimal.ZERO);
            summary.setAvgSessionsPerCase(BigDecimal.ZERO);
            summary.setAvgHoursPerCase(BigDecimal.ZERO);
            summary.setProblemTypeDistribution("{}");
            summary.setClosingReasonDistribution("{}");
            summary.setRiskLevelDistribution("{}");
            return summary;
        }

        // 状态统计
        summary.setApprovedCount(allRecords.stream().filter(r -> "已审核".equals(r.getStatus())).count());
        summary.setDraftCount(allRecords.stream().filter(r -> "草稿".equals(r.getStatus())).count());
        summary.setRejectedCount(allRecords.stream().filter(r -> "已驳回".equals(r.getStatus())).count());

        // 咨询次数与时长
        long totalSessions = allRecords.stream()
                .mapToLong(r -> r.getTotalSessions() != null ? r.getTotalSessions() : 0).sum();
        BigDecimal totalHours = allRecords.stream()
                .map(r -> r.getTotalHours() != null ? r.getTotalHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalSessions(totalSessions);
        summary.setTotalHours(totalHours);
        summary.setAvgSessionsPerCase(
                BigDecimal.valueOf(totalSessions).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        summary.setAvgHoursPerCase(
                totalHours.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));

        // 分布统计
        summary.setProblemTypeDistribution(toJson(countByProblemType(allRecords)));
        summary.setClosingReasonDistribution(toJson(countByField(allRecords, ClosingReport::getClosingReason, "未知")));
        summary.setRiskLevelDistribution(toJson(countByField(allRecords, ClosingReport::getRiskLevel, "低")));

        return summary;
    }

    // ==================== 问题类型统计 ====================

    @Override
    public List<ProblemTypeStatsDTO> getProblemTypeStats(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<ClosingReport> wrapper = buildQueryWrapper(queryDTO);
        List<ClosingReport> allRecords = closingReportMapper.selectList(wrapper);

        long total = allRecords.size();
        if (total == 0) return Collections.emptyList();

        Map<Integer, List<ClosingReport>> grouped = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getProblemType() != null ? r.getProblemType() : 0));

        List<ProblemTypeStatsDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, List<ClosingReport>> entry : grouped.entrySet()) {
            int typeCode = entry.getKey();
            List<ClosingReport> reports = entry.getValue();
            long count = reports.size();

            BigDecimal percentage = BigDecimal.valueOf(count)
                    .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);

            double avgSessions = reports.stream()
                    .mapToInt(r -> r.getTotalSessions() != null ? r.getTotalSessions() : 0).average().orElse(0);

            BigDecimal typeTotalHours = reports.stream()
                    .map(r -> r.getTotalHours() != null ? r.getTotalHours() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avgHours = typeTotalHours.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

            result.add(new ProblemTypeStatsDTO(
                    typeCode, getProblemTypeName(typeCode), count, percentage,
                    BigDecimal.valueOf(avgSessions).setScale(2, RoundingMode.HALF_UP), avgHours));
        }

        result.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return result;
    }

    // ==================== 咨询师统计 ====================

    @Override
    public List<CounselorStatsDTO> getCounselorStats(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<ClosingReport> wrapper = buildQueryWrapper(queryDTO);
        List<ClosingReport> allRecords = closingReportMapper.selectList(wrapper);

        Map<Long, List<ClosingReport>> grouped = allRecords.stream()
                .collect(Collectors.groupingBy(r -> r.getCounselorId() != null ? r.getCounselorId() : 0L));

        List<CounselorStatsDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<ClosingReport>> entry : grouped.entrySet()) {
            Long counselorId = entry.getKey();
            List<ClosingReport> reports = entry.getValue();
            long totalReports = reports.size();
            long closedCount = reports.stream()
                    .filter(r -> "目标达成".equals(r.getClosingReason()) ||
                                 "来访者主动结束".equals(r.getClosingReason())).count();
            long dropoutCount = reports.stream()
                    .filter(r -> "失约终止".equals(r.getClosingReason())).count();

            Map<String, Long> problemDist = reports.stream()
                    .collect(Collectors.groupingBy(
                            r -> getProblemTypeName(r.getProblemType()), Collectors.counting()));

            BigDecimal totalHours = reports.stream()
                    .map(r -> r.getTotalHours() != null ? r.getTotalHours() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new CounselorStatsDTO(counselorId, totalReports, closedCount,
                    dropoutCount, totalHours, toJson(problemDist)));
        }

        result.sort((a, b) -> Long.compare(b.getTotalReports(), a.getTotalReports()));
        return result;
    }

    // ==================== Excel 导出 ====================

    @Override
    public void exportExcel(StatisticsQueryDTO queryDTO, HttpServletResponse response) {
        LambdaQueryWrapper<ClosingReport> wrapper = buildQueryWrapper(queryDTO);
        List<ClosingReport> records = closingReportMapper.selectList(wrapper);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=closing_report_export.xlsx");

        try {
            ExcelExportUtil.exportClosingReport(response.getOutputStream(), records);
            log.info("Excel导出成功，共{}条记录", records.size());
        } catch (Exception e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }

    @Override
    public void batchDownloadZip(List<Long> ids, HttpServletResponse response) {
        if (ids == null || ids.isEmpty()) {
            throw new org.example.common.exception.BusinessException("请选择要下载的报告");
        }
        List<ClosingReport> reports = closingReportMapper.selectBatchIds(ids);
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=closing-reports.zip");
        try {
            ZipDownloadUtil.downloadReportsZip(response.getOutputStream(), reports, reportFilesDir);
            log.info("批量下载结案报告 {} 份", reports.size());
        } catch (Exception e) {
            log.error("批量下载失败", e);
            throw new RuntimeException("批量下载失败: " + e.getMessage());
        }
    }

    // ==================== 私有构建方法 ====================

    private LambdaQueryWrapper<ClosingReport> buildQueryWrapper(StatisticsQueryDTO q) {
        LambdaQueryWrapper<ClosingReport> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(q.getStudentNo()), ClosingReport::getStudentNo, q.getStudentNo())
                .like(StringUtils.hasText(q.getStudentName()), ClosingReport::getStudentName, q.getStudentName())
                .eq(StringUtils.hasText(q.getGender()), ClosingReport::getGender, q.getGender())
                .eq(StringUtils.hasText(q.getDepartment()), ClosingReport::getDepartment, q.getDepartment())
                .like(StringUtils.hasText(q.getStudentGrade()), ClosingReport::getStudentGrade, q.getStudentGrade())
                .eq(q.getCounselorId() != null, ClosingReport::getCounselorId, q.getCounselorId())
                .eq(q.getProblemType() != null, ClosingReport::getProblemType, q.getProblemType())
                .eq(StringUtils.hasText(q.getConsultationMethod()), ClosingReport::getConsultationMethod, q.getConsultationMethod())
                .eq(StringUtils.hasText(q.getStatus()), ClosingReport::getStatus, q.getStatus())
                .eq(StringUtils.hasText(q.getClosingReason()), ClosingReport::getClosingReason, q.getClosingReason())
                .eq(StringUtils.hasText(q.getRiskLevel()), ClosingReport::getRiskLevel, q.getRiskLevel())
                .ge(q.getFirstConsultationStart() != null, ClosingReport::getFirstConsultationDate, q.getFirstConsultationStart())
                .le(q.getFirstConsultationEnd() != null, ClosingReport::getFirstConsultationDate, q.getFirstConsultationEnd())
                .ge(q.getClosingDateStart() != null, ClosingReport::getClosingDate, q.getClosingDateStart())
                .le(q.getClosingDateEnd() != null, ClosingReport::getClosingDate, q.getClosingDateEnd());

        wrapper.orderByDesc(ClosingReport::getCreateTime);
        return wrapper;
    }

    private String getProblemTypeName(Integer code) {
        return code != null && code > 0 && code < PROBLEM_TYPE_NAMES.length
                ? PROBLEM_TYPE_NAMES[code] : "未知";
    }

    private Map<String, Long> countByProblemType(List<ClosingReport> records) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        r -> getProblemTypeName(r.getProblemType()), Collectors.counting()));
    }

    private Map<String, Long> countByField(List<ClosingReport> records,
                                           java.util.function.Function<ClosingReport, String> getter,
                                           String defaultVal) {
        return records.stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            String val = getter.apply(r);
                            return StringUtils.hasText(val) ? val : defaultVal;
                        }, Collectors.counting()));
    }

    private String toJson(Map<String, Long> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Long> e : map.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}
