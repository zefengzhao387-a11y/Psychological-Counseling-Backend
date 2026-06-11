package org.example.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

/**
 * 心语碎片实体 — AI 生成的个性化治愈内容
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HealingFragment extends BaseEntity {

    /** 学生用户ID */
    private Long studentId;

    /** 心情等级：1-很差 2-不太好 3-一般 4-不错 5-很好 */
    private Integer moodLevel;

    /** 学生心情笔记 */
    private String note;

    /** AI 生成的治愈碎片内容 */
    private String fragmentContent;

    /** 是否已查看 */
    private Integer isRead;
}
