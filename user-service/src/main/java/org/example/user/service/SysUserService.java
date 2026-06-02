package org.example.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.user.dto.LoginDTO;
import org.example.user.dto.LoginVO;
import org.example.user.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    /**
     * 登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 根据学号/工号查询
     */
    SysUser getByUserNo(String userNo);
}
