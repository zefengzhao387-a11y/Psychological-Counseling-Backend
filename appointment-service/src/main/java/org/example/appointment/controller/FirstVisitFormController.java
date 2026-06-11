package org.example.appointment.controller;

import org.example.appointment.dto.ConsentConfirmDTO;
import org.example.appointment.entity.FirstVisitForm;
import org.example.appointment.service.FirstVisitFormService;
import org.example.common.context.UserContext;
import org.example.common.feign.dto.StudentProfileBriefDTO;
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
    public R<Void> confirmConsent(@PathVariable Long id, @RequestBody(required = false) ConsentConfirmDTO dto) {
        String signature = dto != null ? dto.getSignature() : null;
        formService.confirmConsent(id, UserContext.getUserId(), signature);
        return R.ok();
    }

    /** 学生查询自己最新的登记表 */
    @GetMapping("/latest")
    public R<FirstVisitForm> latest() {
        FirstVisitForm form = formService.getLatestByStudentId(UserContext.getUserId());
        return R.ok(form);
    }

    /** 微服务内部：按学生 ID 获取登记表中的姓名学号 */
    @GetMapping("/student/{studentId}/profile")
    public R<StudentProfileBriefDTO> studentProfile(@PathVariable Long studentId) {
        FirstVisitForm form = formService.getLatestByStudentId(studentId);
        if (form == null) {
            return R.ok(null);
        }
        StudentProfileBriefDTO profile = new StudentProfileBriefDTO();
        profile.setStudentId(form.getStudentId());
        profile.setStudentName(form.getStudentName());
        profile.setStudentNo(form.getStudentNo());
        return R.ok(profile);
    }

    /** 查询登记表详情（仅本人） */
    @GetMapping("/{id}")
    public R<FirstVisitForm> detail(@PathVariable Long id) {
        return R.ok(formService.getOwnedDetail(id, UserContext.getUserId()));
    }
}
