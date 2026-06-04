package org.example.consultation.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * 追加咨询时段申请
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsultationExtension extends BaseEntity {

    private Long appointmentId;
    /** 申请人ID（咨询师） */
    private Long counselorId;
    /** 申请追加周数 */
    private Integer extendWeeks;
    private String reason;
    /** 状态：1待审批 2已通过 3已拒绝 */
    private Integer status;
    private Long approverId;
    private LocalDateTime approveTime;
    private String approveRemark;
}
