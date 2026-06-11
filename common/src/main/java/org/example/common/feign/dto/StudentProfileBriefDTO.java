package org.example.common.feign.dto;

import lombok.Data;

/**
 * 学生简要信息（跨服务展示用）
 */
@Data
public class StudentProfileBriefDTO {

    private Long studentId;
    private String studentName;
    private String studentNo;
}
