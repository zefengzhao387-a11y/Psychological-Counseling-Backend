package org.example.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询师/初访员列表/详情响应 VO
 */
@Data
public class CounselorInfoVO {

    private Long id;
    private Long userId;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private Integer type;
    private String qualification;
    private String specialty;
    private Integer status;
    private LocalDateTime createTime;
}
