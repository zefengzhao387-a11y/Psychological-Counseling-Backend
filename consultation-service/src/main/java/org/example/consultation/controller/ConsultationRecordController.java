package org.example.consultation.controller;

import org.example.common.context.UserContext;
import org.example.common.result.R;
import org.example.consultation.dto.RecordDTO;
import org.example.consultation.entity.ConsultationRecord;
import org.example.consultation.service.ConsultationRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 咨询记录（咨询师端）
 */
@RestController
@RequestMapping("/api/v1/consultation/record")
public class ConsultationRecordController {

    private final ConsultationRecordService service;

    public ConsultationRecordController(ConsultationRecordService service) {
        this.service = service;
    }

    /** 咨询师录入咨询记录 */
    @PostMapping
    public R<ConsultationRecord> record(@RequestBody RecordDTO dto) {
        return R.ok(service.record(UserContext.getUserId(), dto));
    }

    /** 查看某安排的咨询记录 */
    @GetMapping("/{appointmentId}")
    public R<List<ConsultationRecord>> listByAppointment(@PathVariable Long appointmentId) {
        return R.ok(service.listByAppointment(appointmentId));
    }
}
