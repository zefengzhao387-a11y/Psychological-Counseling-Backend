package org.example.consultation.controller;

import org.example.common.feign.dto.AppointmentProgressDTO;
import org.example.common.result.R;
import org.example.consultation.service.ConsultationProgressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/consultation/progress")
public class ConsultationProgressController {

    private final ConsultationProgressService service;

    public ConsultationProgressController(ConsultationProgressService service) {
        this.service = service;
    }

    /** 按初访预约 ID 批量查询咨询/结案进度（管理员、appointment-service Feign 调用） */
    @GetMapping("/by-appointments")
    public R<List<AppointmentProgressDTO>> listByAppointments(@RequestParam(required = false) String ids) {
        if (ids == null || ids.isBlank()) {
            return R.ok(Collections.emptyList());
        }
        List<Long> appointmentIds = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return R.ok(service.listByAppointmentIds(appointmentIds));
    }
}
