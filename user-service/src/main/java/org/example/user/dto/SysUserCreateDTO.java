package org.example.user.dto;

import lombok.Data;

/**
 * 用户创建/更新请求 DTO
 */
@Data
public class SysUserCreateDTO {

    private String userNo;
    private String username;
    private String password;
    private String phone;
    private String gender;
    private String department;
    private Integer roleCode;
}
