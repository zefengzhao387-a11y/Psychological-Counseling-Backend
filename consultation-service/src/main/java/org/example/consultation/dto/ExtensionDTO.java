package org.example.consultation.dto;

import lombok.Data;

/**
 * 咨询师 — 申请追加时段 DTO
 */
@Data
public class ExtensionDTO {

    private Long appointmentId;
    /** 申请追加周数 */
    private Integer extendWeeks;
    private String reason;
}
