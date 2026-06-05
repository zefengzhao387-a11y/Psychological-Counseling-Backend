package org.example.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户列表/详情响应 VO
 */
@Data
public class SysUserVO {

    private Long id;
    private String userNo;
    private String username;
    private String phone;
    private String gender;
    private String department;
    private Integer roleCode;
    private String roleName;
    private LocalDateTime createTime;
}
