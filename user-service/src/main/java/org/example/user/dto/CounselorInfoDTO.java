package org.example.user.dto;

import lombok.Data;

/**
 * 咨询师/初访员创建/更新请求 DTO
 */
@Data
public class CounselorInfoDTO {

    private Long userId;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private Integer type;
    private String qualification;
    private String specialty;
    private Integer status;
}
