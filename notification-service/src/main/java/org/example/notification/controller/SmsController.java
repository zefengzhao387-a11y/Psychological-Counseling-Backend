package org.example.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.common.result.PageResult;
import org.example.common.result.R;
import org.example.notification.constant.NotificationConstants;
import org.example.notification.dto.SmsSendDTO;
import org.example.notification.entity.SmsLog;
import org.example.notification.service.SmsService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * 短信通知接口（同步发送 + MQ 异步投递）
 */
@Tag(name = "短信通知")
@RestController
@RequestMapping("/api/v1/notification/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;

    @Value("${notification.mq.enabled:false}")
    private boolean mqEnabled;

    @Operation(summary = "同步发送短信")
    @PostMapping("/send")
    public R<SmsLog> send(@RequestBody SmsSendDTO dto) {
        return R.ok("发送成功", smsService.send(dto));
    }

    @Operation(summary = "异步投递短信（RabbitMQ，需 notification.mq.enabled=true）")
    @PostMapping("/publish")
    public R<String> publish(@RequestBody SmsSendDTO dto) {
        if (!mqEnabled) {
            smsService.send(dto);
            return R.ok("MQ 未启用，已同步发送", "sync");
        }
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            smsService.send(dto);
            return R.ok("RabbitMQ 不可用，已同步发送", "sync");
        }
        rabbitTemplate.convertAndSend(
                NotificationConstants.EXCHANGE,
                NotificationConstants.SMS_ROUTING_KEY,
                dto);
        return R.ok("已投递到消息队列", "queued");
    }

    @Operation(summary = "短信发送记录")
    @GetMapping("/logs")
    public R<PageResult<SmsLog>> logs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<SmsLog> result = smsService.pageLogs(page, size);
        return R.ok(PageResult.of(result));
    }
}
