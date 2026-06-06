package org.example.notification.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.common.exception.BusinessException;
import org.example.notification.dto.SmsSendDTO;
import org.example.notification.entity.SmsLog;
import org.example.notification.mapper.SmsLogMapper;
import org.example.notification.service.SmsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class SmsServiceImpl extends ServiceImpl<SmsLogMapper, SmsLog> implements SmsService {

    @Override
    public SmsLog send(SmsSendDTO dto) {
        if (!StringUtils.hasText(dto.getPhone()) || !StringUtils.hasText(dto.getContent())) {
            throw new BusinessException("手机号和短信内容不能为空");
        }

        SmsLog record = new SmsLog();
        record.setPhone(dto.getPhone());
        record.setContent(dto.getContent());
        record.setTemplateCode(dto.getTemplateCode());
        record.setSendStatus(0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        save(record);

        try {
            // 占位实现：打印日志模拟短信网关，张奇奇可替换为阿里云/腾讯云 SDK
            log.info("[SMS] to={} template={} content={}", dto.getPhone(), dto.getTemplateCode(), dto.getContent());
            record.setSendStatus(1);
            record.setSendTime(LocalDateTime.now());
        } catch (Exception e) {
            record.setSendStatus(2);
            record.setFailReason(e.getMessage());
            log.warn("[SMS] send failed: {}", e.getMessage());
        }
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);
        return record;
    }

    @Override
    public Page<SmsLog> pageLogs(Integer page, Integer size) {
        return lambdaQuery().orderByDesc(SmsLog::getCreateTime).page(new Page<>(page, size));
    }
}
