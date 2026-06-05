package org.example.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.common.result.PageResult;
import org.example.common.result.R;
import org.example.user.dto.SysUserCreateDTO;
import org.example.user.dto.SysUserQueryDTO;
import org.example.user.dto.SysUserVO;
import org.example.user.service.SysUserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 CRUD（管理员端）
 */
@RestController
@RequestMapping("/api/v1/user/users")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /** 分页查询用户列表 */
    @GetMapping
    public R<PageResult<SysUserVO>> list(SysUserQueryDTO queryDTO) {
        Page<SysUserVO> page = sysUserService.pageUsers(queryDTO);
        return R.ok(PageResult.of(page));
    }

    /** 查询用户详情 */
    @GetMapping("/{id}")
    public R<SysUserVO> detail(@PathVariable Long id) {
        return R.ok(sysUserService.getUserById(id));
    }

    /** 新增用户 */
    @PostMapping
    public R<String> create(@RequestBody SysUserCreateDTO dto) {
        sysUserService.createUser(dto);
        return R.ok("新增成功");
    }

    /** 更新用户 */
    @PutMapping("/{id}")
    public R<String> update(@PathVariable Long id, @RequestBody SysUserCreateDTO dto) {
        sysUserService.updateUser(id, dto);
        return R.ok("更新成功");
    }

    /** 删除用户 */
    @DeleteMapping("/{id}")
    public R<String> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return R.ok("删除成功");
    }
}
