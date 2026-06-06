package org.example.statistics.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.example.statistics.dto.CounselorStatsDTO;
import org.example.statistics.dto.ProblemTypeStatsDTO;
import org.example.statistics.dto.StatisticsQueryDTO;
import org.example.statistics.dto.SummaryStatsDTO;
import org.example.statistics.entity.ClosingReport;

import java.util.List;

/**
 * 统计分析 Service 接口
 */
public interface StatisticsService {

    /**
     * 多条件筛选查询（分页）
     */
    Page<ClosingReport> filterQuery(StatisticsQueryDTO queryDTO);

    /**
     * 综合汇总统计
     */
    SummaryStatsDTO getSummaryStats(StatisticsQueryDTO queryDTO);

    /**
     * 问题类型分布统计
     */
    List<ProblemTypeStatsDTO> getProblemTypeStats(StatisticsQueryDTO queryDTO);

    /**
     * 咨询师工作量统计
     */
    List<CounselorStatsDTO> getCounselorStats(StatisticsQueryDTO queryDTO);

    /**
     * 导出 Excel
     */
    void exportExcel(StatisticsQueryDTO queryDTO, HttpServletResponse response);

    /**
     * 批量下载结案报告 Word（Zip）
     */
    void batchDownloadZip(java.util.List<Long> ids, HttpServletResponse response);
}
