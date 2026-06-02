package org.example.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应 VO
 */
@Data
@Builder
public class LoginVO {

    private Long userId;
    private String username;
    private String userNo;
    private Integer roleCode;
    private String roleName;
    private String token;
}
