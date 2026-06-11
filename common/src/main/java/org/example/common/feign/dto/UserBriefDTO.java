package org.example.common.feign.dto;

import lombok.Data;

/**
 * 用户简要信息（跨服务 Feign 调用）
 */
@Data
public class UserBriefDTO {

    private Long id;
    private String userNo;
    private String username;
    private String phone;
    private String gender;
    private String department;
    private Integer roleCode;
}
