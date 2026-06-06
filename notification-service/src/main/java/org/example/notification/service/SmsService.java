package org.example.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.notification.dto.SmsSendDTO;
import org.example.notification.entity.SmsLog;

public interface SmsService extends IService<SmsLog> {

    /** 同步发送（写入日志并模拟发送，后续可对接真实短信网关） */
    SmsLog send(SmsSendDTO dto);

    /** 分页查询发送记录 */
    Page<SmsLog> pageLogs(Integer page, Integer size);
}
