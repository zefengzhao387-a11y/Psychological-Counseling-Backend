package org.example.consultation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.common.context.UserContext;
import org.example.common.result.PageResult;
import org.example.common.result.R;
import org.example.consultation.dto.ClosingReportQueryDTO;
import org.example.consultation.dto.ClosingReportSaveDTO;
import org.example.consultation.entity.ClosingReport;
import org.example.consultation.service.ClosingReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 结案报告 Controller（咨询师端 + 管理员端统计用）
 *
 * <p>RESTful API：</p>
 * <ul>
 *   <li>GET    /api/v1/consultation/report/list     — 分页查询</li>
 *   <li>GET    /api/v1/consultation/report/{id}      — 查询详情</li>
 *   <li>POST   /api/v1/consultation/report           — 新增报告</li>
 *   <li>POST   /api/v1/consultation/report/submit    — 咨询师提交（含Word生成）</li>
 *   <li>PUT    /api/v1/consultation/report/{id}      — 修改报告</li>
 *   <li>DELETE /api/v1/consultation/report/{id}      — 删除报告</li>
 *   <li>POST   /api/v1/consultation/report/{id}/word — 生成Word</li>
 *   <li>GET    /api/v1/consultation/report           — 简单条件查询</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/consultation/report")
public class ClosingReportController {

    private final ClosingReportService service;

    public ClosingReportController(ClosingReportService service) {
        this.service = service;
    }

    // ==================== 查询 ====================

    /**
     * 分页查询结案报告列表（支持多条件筛选）
     */
    @GetMapping("/list")
    public R<PageResult<ClosingReport>> list(ClosingReportQueryDTO queryDTO) {
        Page<ClosingReport> page = service.pageQuery(queryDTO);
        return R.ok(PageResult.of(page));
    }

    /**
     * 根据ID查询结案报告详情
     */
    @GetMapping("/{id}")
    public R<ClosingReport> getById(@PathVariable Long id) {
        return R.ok(service.getDetailById(id));
    }

    /**
     * 简单条件查询（兼容旧接口）
     */
    @GetMapping
    public R<List<ClosingReport>> query(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) Integer problemType) {
        return R.ok(service.query(studentNo, studentName, counselorId, problemType));
    }

    // ==================== 新增 ====================

    /**
     * 新增结案报告
     */
    @PostMapping
    public R<ClosingReport> create(@RequestBody ClosingReportSaveDTO saveDTO) {
        return R.ok("新增成功", service.create(saveDTO));
    }

    /**
     * 咨询师提交结案报告（提交后自动生成 Word）
     */
    @PostMapping("/submit")
    public R<ClosingReport> submit(@RequestBody ClosingReport report) {
        return R.ok("提交成功，Word已生成", service.submit(UserContext.getUserId(), report));
    }

    // ==================== 修改 ====================

    /**
     * 修改结案报告
     */
    @PutMapping("/{id}")
    public R<ClosingReport> update(@PathVariable Long id, @RequestBody ClosingReportSaveDTO saveDTO) {
        return R.ok("修改成功", service.update(id, saveDTO));
    }

    // ==================== 删除 ====================

    /**
     * 删除结案报告（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return R.ok("删除成功");
    }

    // ==================== Word 生成 ====================

    /**
     * 生成 Word 文档
     */
    @PostMapping("/{id}/word")
    public R<String> generateWord(@PathVariable Long id) {
        return R.ok("Word已生成", service.generateWord(id));
    }
}
