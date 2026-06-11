package org.example.user.dto;

import lombok.Data;

/**
 * 生成心语碎片请求
 */
@Data
public class GenerateFragmentDTO {

    /** 心情等级 1-5 */
    private Integer moodLevel;

    /** 心情笔记（可选，最多200字） */
    private String note;
}
