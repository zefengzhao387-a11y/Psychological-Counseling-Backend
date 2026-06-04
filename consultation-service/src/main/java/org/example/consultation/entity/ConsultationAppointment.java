package org.example.consultation.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 咨询安排记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsultationAppointment extends BaseEntity {

    private Long studentId;
    private Long firstVisitResultId;
    /** 咨询师ID */
    private Long counselorId;
    /** 咨询开始日期 */
    private LocalDate startDate;
    /** 时间段ID */
    private Long timeSlotId;
    /** 每周几（1-7） */
    private Integer dayOfWeek;
    /** 咨询地点 */
    private String location;
    /** 占用总周数 */
    private Integer occupiedWeeks;
    /** 剩余周数 */
    private Integer remainingWeeks;
    /** 状态：1进行中 2已结案 3已脱落 */
    private Integer status;
    /** 短信通知时间 */
    private LocalDateTime notifyTime;
}
