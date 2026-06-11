package org.example.appointment.controller;

import org.example.appointment.dto.BatchDutyDTO;
import org.example.appointment.entity.DutySchedule;
import org.example.appointment.service.DutyScheduleService;
import org.example.common.result.R;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 值班管理（管理员）
 */
@RestController
@RequestMapping("/api/v1/appointment/duty-schedule")
public class DutyScheduleController {

    private final DutyScheduleService dutyScheduleService;

    public DutyScheduleController(DutyScheduleService dutyScheduleService) {
        this.dutyScheduleService = dutyScheduleService;
    }

    /** 批量排班 */
    @PostMapping("/batch")
    public R<String> batchCreate(@RequestBody BatchDutyDTO dto) {
        dutyScheduleService.batchCreate(dto.getCounselorId(), dto.getCounselorType(),
                dto.getStartDate(), dto.getEndDate(), dto.getTimeSlotIds(), dto.getMaxAppointments());
        return R.ok("排班成功");
    }

    /** 查询某日值班 */
    @GetMapping
    public R<List<DutySchedule>> listByDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return R.ok(dutyScheduleService.listByDate(date));
    }

    /** 查询某日某时段空闲老师 */
    @GetMapping("/available")
    public R<List<DutySchedule>> listAvailable(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Long timeSlotId,
            @RequestParam(required = false) Integer counselorType) {
        return R.ok(dutyScheduleService.listAvailable(date, timeSlotId, counselorType));
    }

    /** 删除某条值班 */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dutyScheduleService.removeById(id);
        return R.ok();
    }
}
