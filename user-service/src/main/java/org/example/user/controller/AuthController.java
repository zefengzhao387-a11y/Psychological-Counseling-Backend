package org.example.user.controller;

import org.example.common.result.R;
import org.example.user.dto.LoginDTO;
import org.example.user.dto.LoginVO;
import org.example.user.service.SysUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/auth")
public class AuthController {

    private final SysUserService sysUserService;

    public AuthController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO vo = sysUserService.login(loginDTO);
        return R.ok("登录成功", vo);
    }

    /**
     * 注册（学生自助注册）
     */
    @PostMapping("/register")
    public R<Void> register(@RequestBody LoginDTO loginDTO) {
        // TODO: 学生注册逻辑（后续补充）
        return R.ok();
    }
}
