package org.example.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.user.dto.*;
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

    /**
     * 分页查询用户列表
     */
    Page<SysUserVO> pageUsers(SysUserQueryDTO queryDTO);

    /**
     * 根据ID查询用户
     */
    SysUserVO getUserById(Long id);

    /**
     * 新增用户
     */
    void createUser(SysUserCreateDTO dto);

    /**
     * 更新用户
     */
    void updateUser(Long id, SysUserCreateDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(Long id);
}
