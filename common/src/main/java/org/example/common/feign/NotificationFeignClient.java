package org.example.common.feign;

import org.example.common.feign.dto.SmsSendRequest;
import org.example.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 通知服务 Feign 客户端
 */
@FeignClient(name = "notification-service", url = "${service.notification-url:http://localhost:8085}")
public interface NotificationFeignClient {

    @PostMapping("/api/v1/notification/sms/publish")
    R<?> publishSms(@RequestBody SmsSendRequest request);
}
