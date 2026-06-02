package org.example.appointment.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 批量排班 DTO
 */
@Data
public class BatchDutyDTO {

    private Long counselorId;
    private Integer counselorType; // 1初访员 2咨询师
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> timeSlotIds;
    private Integer maxAppointments;
}
