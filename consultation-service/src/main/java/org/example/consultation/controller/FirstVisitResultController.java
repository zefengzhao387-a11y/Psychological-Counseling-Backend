package org.example.consultation.controller;

import org.example.common.context.UserContext;
import org.example.common.result.R;
import org.example.consultation.entity.FirstVisitResult;
import org.example.consultation.service.FirstVisitResultService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 初访管理（初访员端）
 */
@RestController
@RequestMapping("/api/v1/consultation/result")
public class FirstVisitResultController {

    private final FirstVisitResultService service;

    public FirstVisitResultController(FirstVisitResultService service) {
        this.service = service;
    }

    /** 初访员录入评估结果 */
    @PostMapping
    public R<FirstVisitResult> record(@RequestBody FirstVisitResult result) {
        return R.ok(service.record(UserContext.getUserId(), result));
    }

    /** 心理助理查看待安排列表 */
    @GetMapping("/pending")
    public R<List<FirstVisitResult>> listPending() {
        return R.ok(service.listPendingArrangement());
    }

    /** 心理助理全部待办（含无需咨询/转介） */
    @GetMapping("/assistant-tasks")
    public R<List<FirstVisitResult>> listAssistantTasks() {
        return R.ok(service.listAssistantTasks());
    }

    /** 标记已处理（无需咨询/转介） */
    @PutMapping("/{id}/mark-processed")
    public R<Void> markProcessed(@PathVariable Long id) {
        service.markProcessed(id);
        return R.ok();
    }

    /** 初访员查看自己的评估记录 */
    @GetMapping("/my")
    public R<List<FirstVisitResult>> listMy() {
        return R.ok(service.listMyByVisitor(UserContext.getUserId()));
    }

    /** 学生查看自己的初访评估结果 */
    @GetMapping("/student/my")
    public R<List<FirstVisitResult>> listStudentMy() {
        return R.ok(service.listByStudent(UserContext.getUserId()));
    }

    /** 初访员已评估的预约 ID（appointment-service 过滤待办用） */
    @GetMapping("/evaluated-appointment-ids")
    public R<List<Long>> listEvaluatedAppointmentIds(@RequestParam(required = false) Long visitorId) {
        Long id = visitorId != null ? visitorId : UserContext.getUserId();
        return R.ok(service.listEvaluatedAppointmentIds(id));
    }

    /** 服务间调用：全部已评估预约 ID */
    @GetMapping("/internal/evaluated-appointment-ids")
    public R<List<Long>> listAllEvaluatedAppointmentIds() {
        return R.ok(service.listAllEvaluatedAppointmentIds());
    }
}
