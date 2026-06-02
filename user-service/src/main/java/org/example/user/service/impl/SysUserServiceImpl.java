package org.example.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.enums.UserRole;
import org.example.common.exception.BusinessException;
import org.example.common.utils.JwtUtil;
import org.example.user.dto.LoginDTO;
import org.example.user.dto.LoginVO;
import org.example.user.entity.SysUser;
import org.example.user.mapper.SysUserMapper;
import org.example.user.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        SysUser user = getByUserNo(loginDTO.getUserNo());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 校验密码（MD5 简单加密）
        String md5Password = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!md5Password.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        // 生成 token
        String token = JwtUtil.generate(user.getId(), user.getUsername(), user.getRoleCode());
        // 获取角色名称
        String roleName = "";
        for (UserRole role : UserRole.values()) {
            if (role.getCode() == user.getRoleCode()) {
                roleName = role.getDesc();
                break;
            }
        }
        return LoginVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .userNo(user.getUserNo())
                .roleCode(user.getRoleCode())
                .roleName(roleName)
                .token(token)
                .build();
    }

    @Override
    public SysUser getByUserNo(String userNo) {
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserNo, userNo));
    }
}
