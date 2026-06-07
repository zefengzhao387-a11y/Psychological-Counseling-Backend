package org.example.consultation.controller;

import org.example.common.context.UserContext;
import org.example.common.result.R;
import org.example.consultation.dto.ApproveDTO;
import org.example.consultation.dto.ExtensionDTO;
import org.example.consultation.entity.ConsultationExtension;
import org.example.consultation.service.ConsultationExtensionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 追加咨询时段（咨询师申请 + 管理员审批）
 */
@RestController
@RequestMapping("/api/v1/consultation/extension")
public class ConsultationExtensionController {

    private final ConsultationExtensionService service;

    public ConsultationExtensionController(ConsultationExtensionService service) {
        this.service = service;
    }

    /** 咨询师提交追加申请 */
    @PostMapping
    public R<ConsultationExtension> apply(@RequestBody ExtensionDTO dto) {
        return R.ok(service.apply(UserContext.getUserId(), dto));
    }

    /** 管理员审批 */
    @PostMapping("/approve")
    public R<Void> approve(@RequestBody ApproveDTO dto) {
        service.approve(UserContext.getUserId(), dto);
        return R.ok();
    }

    /** 待审批列表 */
    @GetMapping("/pending")
    public R<List<ConsultationExtension>> listPending() {
        return R.ok(service.listPending());
    }

    /** 咨询师查看自己的申请列表 */
    @GetMapping("/my")
    public R<List<ConsultationExtension>> listMy() {
        return R.ok(service.listByCounselor(UserContext.getUserId()));
    }
}
