package org.example.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 综合汇总统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 报告总数 */
    private Long totalReports;

    /** 已审核数 */
    private Long approvedCount;

    /** 草稿数 */
    private Long draftCount;

    /** 已驳回数 */
    private Long rejectedCount;

    /** 已完成咨询总次数 */
    private Long totalSessions;

    /** 总咨询时长汇总（小时） */
    private BigDecimal totalHours;

    /** 平均每案咨询次数 */
    private BigDecimal avgSessionsPerCase;

    /** 平均每案咨询时长 */
    private BigDecimal avgHoursPerCase;

    /** 各问题类型数量分布（JSON格式） */
    private String problemTypeDistribution;

    /** 各结案原因数量分布（JSON格式） */
    private String closingReasonDistribution;

    /** 各风险等级数量分布（JSON格式） */
    private String riskLevelDistribution;
}
