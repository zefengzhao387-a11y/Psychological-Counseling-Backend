package org.example.notification.dto;

import lombok.Data;

/**
 * 短信发送请求
 */
@Data
public class SmsSendDTO {

    private String phone;

    private String content;

    /** 模板编码：APPOINTMENT_SUCCESS / APPOINTMENT_CHANGE / CONSULTATION_ARRANGE / REMINDER */
    private String templateCode;
}
