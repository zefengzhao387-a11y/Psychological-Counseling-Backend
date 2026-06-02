package org.example.appointment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.appointment.entity.FirstVisitForm;

public interface FirstVisitFormService extends IService<FirstVisitForm> {

    /** 学生提交首访登记表（含计分逻辑） */
    FirstVisitForm submit(FirstVisitForm form);

    /** 确认知情同意书 */
    void confirmConsent(Long formId, Long studentId);
}
