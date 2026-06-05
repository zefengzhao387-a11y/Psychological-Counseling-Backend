package org.example.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.enums.UserRole;
import org.example.common.exception.BusinessException;
import org.example.common.utils.JwtUtil;
import org.example.user.dto.*;
import org.example.user.entity.SysUser;
import org.example.user.mapper.SysUserMapper;
import org.example.user.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = getByUserNo(loginDTO.getUserNo());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String md5Password = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        if (!md5Password.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = JwtUtil.generate(user.getId(), user.getUsername(), user.getRoleCode());
        String roleName = getRoleName(user.getRoleCode());
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

    @Override
    public Page<SysUserVO> pageUsers(SysUserQueryDTO queryDTO) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getRoleCode() != null) {
            wrapper.eq(SysUser::getRoleCode, queryDTO.getRoleCode());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, queryDTO.getKeyword())
                    .or().like(SysUser::getUserNo, queryDTO.getKeyword()));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        IPage<SysUser> page = page(
                new Page<>(queryDTO.getPage(), queryDTO.getSize()), wrapper);

        Page<SysUserVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return result;
    }

    @Override
    public SysUserVO getUserById(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    @Override
    public void createUser(SysUserCreateDTO dto) {
        SysUser exist = getByUserNo(dto.getUserNo());
        if (exist != null) {
            throw new BusinessException("学号/工号已存在");
        }
        SysUser user = new SysUser();
        user.setUserNo(dto.getUserNo());
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setDepartment(dto.getDepartment());
        user.setRoleCode(dto.getRoleCode());
        save(user);
    }

    @Override
    public void updateUser(Long id, SysUserCreateDTO dto) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser exist = getByUserNo(dto.getUserNo());
        if (exist != null && !exist.getId().equals(id)) {
            throw new BusinessException("学号/工号已被其他用户使用");
        }
        user.setUserNo(dto.getUserNo());
        user.setUsername(dto.getUsername());
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        }
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setDepartment(dto.getDepartment());
        user.setRoleCode(dto.getRoleCode());
        updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRoleCode() == UserRole.CENTER_ADMIN.getCode()) {
            throw new BusinessException("不能删除管理员账号");
        }
        removeById(id);
    }

    private SysUserVO toVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setUserNo(user.getUserNo());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setGender(user.getGender());
        vo.setDepartment(user.getDepartment());
        vo.setRoleCode(user.getRoleCode());
        vo.setRoleName(getRoleName(user.getRoleCode()));
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    private String getRoleName(int roleCode) {
        for (UserRole role : UserRole.values()) {
            if (role.getCode() == roleCode) {
                return role.getDesc();
            }
        }
        return "未知";
    }
}
