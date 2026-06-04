package org.example.consultation.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 咨询师 — 录入咨询记录 DTO
 */
@Data
public class RecordDTO {

    private Long appointmentId;
    private Integer sessionNumber;
    private LocalDate consultDate;
    /** 状态：1完成咨询 2旷约 3请假 4脱落 5结案 */
    private Integer status;
    private String content;
    private String counselorNote;
}
