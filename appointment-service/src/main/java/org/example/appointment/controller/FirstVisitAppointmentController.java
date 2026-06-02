package org.example.appointment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.appointment.dto.*;
import org.example.appointment.service.FirstVisitAppointmentService;
import org.example.common.context.UserContext;
import org.example.common.result.PageResult;
import org.example.common.result.R;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 初访预约（学生 + 管理员共用）
 */
@RestController
@RequestMapping("/api/v1/appointment/first-visit")
public class FirstVisitAppointmentController {

    private final FirstVisitAppointmentService appointmentService;

    public FirstVisitAppointmentController(FirstVisitAppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ==================== 学生端 ====================

    /** 学生提交初访预约 */
    @PostMapping("/submit")
    public R<?> submit(@RequestBody SubmitDTO dto) {
        Long studentId = UserContext.getUserId();
        appointmentService.submit(studentId, dto.getFormId(), dto.getDutyScheduleId(),
                dto.getAppointmentDate(), dto.getTimeSlotId());
        return R.ok("预约提交成功，等待审核");
    }

    /** 学生撤销预约 */
    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        Long studentId = UserContext.getUserId();
        appointmentService.cancel(id, studentId);
        return R.ok();
    }

    /** 学生查看我的预约 */
    @GetMapping("/my")
    public R<PageResult<AppointmentVO>> myAppointments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long studentId = UserContext.getUserId();
        Page<AppointmentVO> result = appointmentService.myAppointments(page, size, studentId);
        return R.ok(PageResult.of(result));
    }

    // ==================== 管理员端 ====================

    /** 管理员审核列表（计分报警排序） */
    @GetMapping("/review-list")
    public R<PageResult<AppointmentVO>> reviewList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<AppointmentVO> result = appointmentService.reviewList(page, size, status);
        return R.ok(PageResult.of(result));
    }

    /** 管理员审核（通过/拒绝） */
    @PostMapping("/review")
    public R<String> review(@RequestBody ReviewDTO dto) {
        Long reviewerId = UserContext.getUserId();
        appointmentService.review(dto, reviewerId);
        return R.ok(dto.getStatus() == 2 ? "已通过，短信已通知学生" : "已拒绝");
    }

    /** 管理员切换优先排队 */
    @PutMapping("/toggle-priority/{id}")
    public R<Void> togglePriority(@PathVariable Long id) {
        appointmentService.togglePriority(id);
        return R.ok();
    }

    /** 管理员改约 */
    @PutMapping("/reschedule")
    public R<Void> reschedule(@RequestBody RescheduleDTO dto) {
        appointmentService.reschedule(dto);
        return R.ok();
    }

    /** 管理员新增预约 */
    @PostMapping("/add")
    public R<String> addAppointment(@RequestBody AppointmentAddDTO dto) {
        Long adminId = UserContext.getUserId();
        appointmentService.addAppointment(dto, adminId);
        return R.ok("新增成功");
    }

    /** 查询某学生今日预约 */
    @GetMapping("/today")
    public R<AppointmentVO> getToday(
            @RequestParam Long studentId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return R.ok(appointmentService.getTodayAppointment(studentId, date));
    }
}
