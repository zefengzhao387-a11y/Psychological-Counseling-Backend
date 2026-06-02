package org.example.appointment.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 改约 DTO
 */
@Data
public class RescheduleDTO {

    /** 预约ID */
    private Long appointmentId;

    /** 新初访员ID */
    private Long visitorId;

    /** 新预约日期 */
    private LocalDate appointmentDate;

    /** 新时间段ID */
    private Long timeSlotId;

    /** 新咨询地点 */
    private String location;
}
