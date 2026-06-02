package org.example.user.dto;

import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginDTO {

    private String userNo;
    private String password;
}
