package org.example.consultation.controller;

import org.example.common.context.UserContext;
import org.example.common.result.R;
import org.example.consultation.entity.ClosingReport;
import org.example.consultation.service.ClosingReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 结案报告（咨询师端 + 管理员端统计用）
 */
@RestController
@RequestMapping("/api/v1/consultation/report")
public class ClosingReportController {

    private final ClosingReportService service;

    public ClosingReportController(ClosingReportService service) {
        this.service = service;
    }

    /** 咨询师提交结案报告（提交后自动生成 Word） */
    @PostMapping
    public R<ClosingReport> submit(@RequestBody ClosingReport report) {
        return R.ok("提交成功，Word已生成", service.submit(UserContext.getUserId(), report));
    }

    /** 查询结案报告列表 */
    @GetMapping
    public R<List<ClosingReport>> query(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) Integer problemType) {
        return R.ok(service.query(studentNo, studentName, counselorId, problemType));
    }

    /** 生成 Word */
    @PostMapping("/{id}/word")
    public R<String> generateWord(@PathVariable Long id) {
        return R.ok("Word已生成", service.generateWord(id));
    }
}
