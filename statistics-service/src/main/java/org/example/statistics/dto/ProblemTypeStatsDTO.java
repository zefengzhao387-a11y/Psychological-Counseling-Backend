package org.example.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 问题类型统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemTypeStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 问题类型编码 */
    private Integer problemType;

    /** 问题类型名称 */
    private String problemTypeName;

    /** 该类型数量 */
    private Long count;

    /** 占比（百分比） */
    private BigDecimal percentage;

    /** 平均咨询次数 */
    private BigDecimal avgSessions;

    /** 平均咨询时长（小时） */
    private BigDecimal avgHours;
}
