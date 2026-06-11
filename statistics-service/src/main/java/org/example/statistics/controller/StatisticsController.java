package org.example.statistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.feign.dto.ClosingReportSyncDTO;
import org.example.common.result.PageResult;
import org.example.common.result.R;
import org.example.statistics.dto.CounselorStatsDTO;
import org.example.statistics.dto.ProblemTypeStatsDTO;
import org.example.statistics.dto.StatisticsQueryDTO;
import org.example.statistics.dto.SummaryStatsDTO;
import org.example.statistics.entity.ClosingReport;
import org.example.statistics.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.statistics.dto.BatchDownloadDTO;

import java.util.List;

/**
 * 统计分析 Controller
 *
 * <p>API 接口：</p>
 * <ul>
 *   <li>GET /api/v1/statistics/list         — 多条件筛选分页查询</li>
 *   <li>GET /api/v1/statistics/summary      — 综合汇总统计</li>
 *   <li>GET /api/v1/statistics/problem-type — 问题类型分布统计</li>
 *   <li>GET /api/v1/statistics/counselor    — 咨询师工作量统计</li>
 *   <li>GET /api/v1/statistics/export       — 导出Excel</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 多条件筛选查询（分页）
     */
    @GetMapping("/list")
    public R<PageResult<ClosingReport>> list(StatisticsQueryDTO queryDTO) {
        log.info("统计分析-筛选查询: studentNo={}, studentName={}, problemType={}, closingDate={}~{}",
                queryDTO.getStudentNo(), queryDTO.getStudentName(),
                queryDTO.getProblemType(), queryDTO.getClosingDateStart(), queryDTO.getClosingDateEnd());
        Page<ClosingReport> page = statisticsService.filterQuery(queryDTO);
        return R.ok(PageResult.of(page));
    }

    /**
     * 综合汇总统计
     */
    @GetMapping("/summary")
    public R<SummaryStatsDTO> summary(StatisticsQueryDTO queryDTO) {
        log.info("统计分析-综合汇总");
        return R.ok(statisticsService.getSummaryStats(queryDTO));
    }

    /**
     * 问题类型分布统计
     */
    @GetMapping("/problem-type")
    public R<List<ProblemTypeStatsDTO>> problemTypeStats(StatisticsQueryDTO queryDTO) {
        log.info("统计分析-问题类型分布");
        return R.ok(statisticsService.getProblemTypeStats(queryDTO));
    }

    /**
     * 咨询师工作量统计
     */
    @GetMapping("/counselor")
    public R<List<CounselorStatsDTO>> counselorStats(StatisticsQueryDTO queryDTO) {
        log.info("统计分析-咨询师工作量");
        return R.ok(statisticsService.getCounselorStats(queryDTO));
    }

    /**
     * 导出 Excel（结案报告数据）
     */
    @GetMapping("/export")
    public void export(StatisticsQueryDTO queryDTO, HttpServletResponse response) {
        log.info("统计分析-导出Excel");
        statisticsService.exportExcel(queryDTO, response);
    }

    /**
     * 导出咨询师统计 Excel
     */
    @GetMapping("/export/counselor")
    public void exportCounselor(StatisticsQueryDTO queryDTO, HttpServletResponse response) {
        log.info("统计分析-导出咨询师统计Excel, 时间范围: {}~{}",
                queryDTO.getClosingDateStart(), queryDTO.getClosingDateEnd());
        statisticsService.exportCounselorExcel(queryDTO, response);
    }

    /**
     * 批量下载结案报告 Word（Zip）
     */
    @PostMapping("/download")
    public void batchDownload(@RequestBody BatchDownloadDTO dto, HttpServletResponse response) {
        log.info("统计分析-批量下载 {} 份报告", dto.getIds() != null ? dto.getIds().size() : 0);
        statisticsService.batchDownloadZip(dto.getIds(), response);
    }

    /**
     * 同步结案报告（consultation-service 内部调用）
     */
    @PostMapping("/report/sync")
    public R<Void> syncReport(@RequestBody ClosingReportSyncDTO dto) {
        statisticsService.syncClosingReport(dto);
        return R.ok();
    }
}
