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
}
