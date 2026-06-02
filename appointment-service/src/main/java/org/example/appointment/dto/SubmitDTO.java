package org.example.appointment.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 学生提交预约 DTO
 */
@Data
public class SubmitDTO {

    private Long formId;
    private Long dutyScheduleId;
    private LocalDate appointmentDate;
    private Long timeSlotId;
}
