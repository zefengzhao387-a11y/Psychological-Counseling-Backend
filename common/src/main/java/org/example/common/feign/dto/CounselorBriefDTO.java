package org.example.common.feign.dto;

import lombok.Data;

/**
 * 咨询师/初访员简要信息（Feign 传输）
 */
@Data
public class CounselorBriefDTO {

    private Long id;
    private Long userId;
    private String name;
    /** 1初访员 2咨询师 */
    private Integer type;
}
