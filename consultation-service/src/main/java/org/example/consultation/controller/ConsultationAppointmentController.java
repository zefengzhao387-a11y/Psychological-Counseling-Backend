package org.example.consultation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.common.result.PageResult;
import org.example.common.result.R;
import org.example.consultation.dto.ArrangeDTO;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.service.ConsultationAppointmentService;
import org.springframework.web.bind.annotation.*;

/**
 * 咨询安排（心理助理端）
 */
@RestController
@RequestMapping("/api/v1/consultation")
public class ConsultationAppointmentController {

    private final ConsultationAppointmentService service;

    public ConsultationAppointmentController(ConsultationAppointmentService service) {
        this.service = service;
    }

    /** 安排咨询（默认占用8周） */
    @PostMapping("/arrange")
    public R<ConsultationAppointment> arrange(@RequestBody ArrangeDTO dto) {
        return R.ok("安排成功，已默认占用8周", service.arrange(dto));
    }

    /** 提前结案释放时段 */
    @PutMapping("/close/{id}")
    public R<Void> closeEarly(@PathVariable Long id) {
        service.closeEarly(id);
        return R.ok();
    }

    /** 咨询安排列表；closableOnly=true 时返回可写结案报告的安排 */
    @GetMapping("/records")
    public R<PageResult<ConsultationAppointment>> listAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) Boolean closableOnly) {
        Page<ConsultationAppointment> result = Boolean.TRUE.equals(closableOnly)
                ? service.listClosableForReport(page, size, counselorId)
                : service.listAll(page, size, counselorId);
        return R.ok(PageResult.of(result));
    }

    /** 改约 */
    @PutMapping("/records/{id}")
    public R<Void> updateArrangement(@PathVariable Long id, @RequestBody ArrangeDTO dto) {
        service.updateArrangement(id, dto);
        return R.ok();
    }

    /** 新增咨询安排（手动补录） */
    @PostMapping("/records")
    public R<ConsultationAppointment> createRecord(@RequestBody ArrangeDTO dto) {
        return R.ok("新增成功", service.arrange(dto));
    }
}
