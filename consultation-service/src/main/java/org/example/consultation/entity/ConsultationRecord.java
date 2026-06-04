package org.example.consultation.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

import java.time.LocalDate;

/**
 * 咨询记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsultationRecord extends BaseEntity {

    private Long appointmentId;
    /** 第几次咨询（1~8, 9+） */
    private Integer sessionNumber;
    private LocalDate consultDate;
    /** 状态：1完成咨询 2旷约 3请假 4脱落 5结案 */
    private Integer status;
    private String content;
    private String counselorNote;
}
