package org.example.common.feign.dto;

import lombok.Data;

/**
 * 短信发送请求（Feign 跨服务调用）
 */
@Data
public class SmsSendRequest {

    private String phone;

    private String content;

    private String templateCode;
}
