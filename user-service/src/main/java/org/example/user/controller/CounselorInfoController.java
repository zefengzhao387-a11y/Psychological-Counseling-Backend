package org.example.user.controller;

import org.example.common.result.R;
import org.example.user.dto.CounselorInfoDTO;
import org.example.user.dto.CounselorInfoVO;
import org.example.user.service.CounselorInfoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 咨询师/初访员信息维护（管理员端）
 */
@RestController
@RequestMapping("/api/v1/user/counselor")
public class CounselorInfoController {

    private final CounselorInfoService counselorInfoService;

    public CounselorInfoController(CounselorInfoService counselorInfoService) {
        this.counselorInfoService = counselorInfoService;
    }

    /** 列表查询 */
    @GetMapping("/list")
    public R<List<CounselorInfoVO>> list() {
        return R.ok(counselorInfoService.listAll());
    }

    /** 新增 */
    @PostMapping
    public R<String> create(@RequestBody CounselorInfoDTO dto) {
        counselorInfoService.create(dto);
        return R.ok("新增成功");
    }

    /** 更新 */
    @PutMapping("/{id}")
    public R<String> update(@PathVariable Long id, @RequestBody CounselorInfoDTO dto) {
        counselorInfoService.update(id, dto);
        return R.ok("更新成功");
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        counselorInfoService.delete(id);
        return R.ok("删除成功");
    }

    /** 根据 userId 查询咨询师/初访员信息 */
    @GetMapping("/by-user/{userId}")
    public R<CounselorInfoVO> getByUserId(@PathVariable Long userId) {
        CounselorInfoVO vo = counselorInfoService.getByUserId(userId);
        return vo != null ? R.ok(vo) : R.fail(404, "未找到");
    }
}
