package org.example.common.enums;

import lombok.Getter;

/**
 * 问题类型枚举
 */
@Getter
public enum ProblemType {

    ACADEMIC(1, "学业问题"),
    EMOTIONAL(2, "情绪问题"),
    INTERPERSONAL(3, "人际关系"),
    LOVE(4, "恋爱问题"),
    CAREER(5, "职业发展"),
    SELF_GROWTH(6, "自我成长"),
    FAMILY(7, "家庭问题"),
    OTHER(8, "其他");

    private final int code;
    private final String desc;

    ProblemType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
