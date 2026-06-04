package org.example.consultation.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 心理助理 — 安排咨询 DTO
 */
@Data
public class ArrangeDTO {

    private Long resultId;
    private Long studentId;
    private Long counselorId;
    private LocalDate startDate;
    private Long timeSlotId;
    private String location;
    /** 占用周数，默认8 */
    private Integer occupiedWeeks;
}
