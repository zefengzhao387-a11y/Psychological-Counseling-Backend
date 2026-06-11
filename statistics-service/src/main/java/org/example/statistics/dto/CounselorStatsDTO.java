package org.example.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 咨询师工作量统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounselorStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 咨询师ID */
    private Long counselorId;

    /** 咨询师姓名 */
    private String counselorName;

    /** 报告总数 */
    private Long totalReports;

    /** 已结案数（目标达成 + 来访者主动结束） */
    private Long closedCount;

    /** 脱落数 */
    private Long dropoutCount;

    /** 总咨询时长汇总（小时） */
    private BigDecimal totalHours;

    /** 各问题类型统计（JSON格式） */
    private String problemTypeBreakdown;
}
