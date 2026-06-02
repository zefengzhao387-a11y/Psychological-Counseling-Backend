package org.example.appointment.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 管理员新增预约 DTO
 */
@Data
public class AppointmentAddDTO {

    /** 学号或姓名（模糊匹配） */
    private String keyword;

    /** 学生ID（精确匹配） */
    private Long studentId;

    /** 首访登记表ID */
    private Long formId;

    /** 初访员ID */
    private Long visitorId;

    /** 预约日期 */
    private LocalDate appointmentDate;

    /** 时间段ID */
    private Long timeSlotId;

    /** 咨询地点 */
    private String location;
}
