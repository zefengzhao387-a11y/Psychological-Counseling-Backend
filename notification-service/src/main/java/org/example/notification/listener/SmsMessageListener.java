package org.example.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.constant.NotificationConstants;
import org.example.notification.dto.SmsSendDTO;
import org.example.notification.service.SmsService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notification.mq", name = "enabled", havingValue = "true")
public class SmsMessageListener {

    private final SmsService smsService;

    @RabbitListener(queues = NotificationConstants.SMS_QUEUE)
    public void onSmsMessage(SmsSendDTO message) {
        log.info("收到短信 MQ 消息: phone={}", message.getPhone());
        smsService.send(message);
    }
}
