package org.example.appointment.controller;

import org.example.appointment.entity.FirstVisitForm;
import org.example.appointment.service.FirstVisitFormService;
import org.example.common.context.UserContext;
import org.example.common.result.R;
import org.springframework.web.bind.annotation.*;

/**
 * 首访登记表（学生端）
 */
@RestController
@RequestMapping("/api/v1/appointment/form")
public class FirstVisitFormController {

    private final FirstVisitFormService formService;

    public FirstVisitFormController(FirstVisitFormService formService) {
        this.formService = formService;
    }

    /** 学生提交首访登记表 */
    @PostMapping
    public R<FirstVisitForm> submit(@RequestBody FirstVisitForm form) {
        form.setStudentId(UserContext.getUserId());
        formService.submit(form);
        return R.ok("提交成功", form);
    }

    /** 学生确认知情同意书 */
    @PutMapping("/{id}/consent")
    public R<Void> confirmConsent(@PathVariable Long id) {
        formService.confirmConsent(id, UserContext.getUserId());
        return R.ok();
    }

    /** 查询登记表详情 */
    @GetMapping("/{id}")
    public R<FirstVisitForm> detail(@PathVariable Long id) {
        return R.ok(formService.getById(id));
    }
}
