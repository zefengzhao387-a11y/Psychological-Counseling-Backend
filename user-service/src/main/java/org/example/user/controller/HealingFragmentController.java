package org.example.user.controller;

import org.example.common.result.R;
import org.example.common.context.UserContext;
import org.example.user.dto.GenerateFragmentDTO;
import org.example.user.dto.HealingFragmentVO;
import org.example.user.service.HealingFragmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 心语碎片接口
 */
@RestController
@RequestMapping("/api/v1/user/healing")
public class HealingFragmentController {

    private final HealingFragmentService healingFragmentService;

    public HealingFragmentController(HealingFragmentService healingFragmentService) {
        this.healingFragmentService = healingFragmentService;
    }

    /** 生成新的治愈碎片 */
    @PostMapping("/generate")
    public R<HealingFragmentVO> generate(@RequestBody GenerateFragmentDTO dto) {
        Long studentId = UserContext.getUserId();
        return R.ok(healingFragmentService.generate(studentId, dto));
    }

    /** 查看我的所有碎片 */
    @GetMapping("/list")
    public R<List<HealingFragmentVO>> list() {
        Long studentId = UserContext.getUserId();
        return R.ok(healingFragmentService.listByStudent(studentId));
    }

    /** 标记已读 */
    @PutMapping("/{fragmentId}/read")
    public R<Void> markRead(@PathVariable Long fragmentId) {
        Long studentId = UserContext.getUserId();
        healingFragmentService.markRead(fragmentId, studentId);
        return R.ok();
    }

    /** 7天心情趋势 */
    @GetMapping("/trend")
    public R<List<HealingFragmentVO>> trend() {
        Long studentId = UserContext.getUserId();
        return R.ok(healingFragmentService.weeklyTrend(studentId));
    }

    /** 删除碎片 */
    @DeleteMapping("/{fragmentId}")
    public R<Void> delete(@PathVariable Long fragmentId) {
        Long studentId = UserContext.getUserId();
        healingFragmentService.delete(fragmentId, studentId);
        return R.ok();
    }
}
