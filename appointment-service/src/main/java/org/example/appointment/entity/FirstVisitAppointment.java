package org.example.appointment.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 初访预约记录
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FirstVisitAppointment extends BaseEntity {

    /** 学生用户ID */
    private Long studentId;

    /** 首访登记表ID */
    private Long formId;

    /** 初访员ID */
    private Long visitorId;

    /** 值班安排ID */
    private Long dutyScheduleId;

    /** 预约日期 */
    private LocalDate appointmentDate;

    /** 时间段ID */
    private Long timeSlotId;

    /** 咨询地点 */
    private String location;

    /** 状态：1待审核 2已通过 3已拒绝 4已撤销 5已完成（初访评估已录入） */
    private Integer status;

    /** 是否优先排队 */
    private Integer isPriority;

    /** 审核人ID */
    private Long reviewerId;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    /** 审核备注 */
    private String reviewRemark;
}
