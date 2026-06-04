package org.example.consultation.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * 初访评估结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FirstVisitResult extends BaseEntity {

    private Long appointmentId;
    private Long studentId;
    private Long visitorId;
    /** 危机等级：1低 2中 3高 4紧急 */
    private Integer crisisLevel;
    /** 问题类型 */
    private Integer problemType;
    /** 初访时间 */
    private LocalDateTime visitTime;
    /** 初访结论：1无需咨询 2安排咨询 3转介送诊 */
    private Integer conclusion;
    private String remark;
}
