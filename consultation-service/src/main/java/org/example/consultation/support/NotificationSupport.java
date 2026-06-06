package org.example.consultation.support;

import lombok.extern.slf4j.Slf4j;
import org.example.common.feign.NotificationFeignClient;
import org.example.common.feign.dto.SmsSendRequest;
import org.springframework.stereotype.Component;

/**
 * 短信通知（Feign 调用 notification-service）
 */
@Slf4j
@Component
public class NotificationSupport {

    private final NotificationFeignClient notificationFeignClient;

    public NotificationSupport(NotificationFeignClient notificationFeignClient) {
        this.notificationFeignClient = notificationFeignClient;
    }

    public void sendSms(String phone, String content, String templateCode) {
        try {
            SmsSendRequest req = new SmsSendRequest();
            req.setPhone(phone);
            req.setContent(content);
            req.setTemplateCode(templateCode);
            notificationFeignClient.publishSms(req);
        } catch (Exception e) {
            log.warn("短信通知发送失败（不影响主流程）: {}", e.getMessage());
        }
    }
}
