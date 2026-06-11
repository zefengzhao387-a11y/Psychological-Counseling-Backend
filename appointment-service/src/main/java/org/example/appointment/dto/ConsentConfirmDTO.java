package org.example.appointment.dto;

import lombok.Data;

/**
 * 知情同意书确认
 */
@Data
public class ConsentConfirmDTO {

    /** 电子签名（姓名） */
    private String signature;
}
