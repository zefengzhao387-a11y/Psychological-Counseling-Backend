package org.example.appointment.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

import java.time.LocalDate;

/**
 * 值班安排
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DutySchedule extends BaseEntity {

    /** 老师ID */
    private Long counselorId;

    /** 老师类型：1初访员 2咨询师 */
    private Integer counselorType;

    /** 值班日期 */
    private LocalDate dutyDate;

    /** 时间段ID */
    private Long timeSlotId;

    /** 该时段最大预约数 */
    private Integer maxAppointments;

    /** 已预约数 */
    private Integer bookedCount;
}
