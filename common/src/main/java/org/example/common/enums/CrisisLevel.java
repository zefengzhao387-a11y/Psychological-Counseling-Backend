package org.example.common.enums;

import lombok.Getter;

/**
 * 危机等级枚举
 */
@Getter
public enum CrisisLevel {

    LOW(1, "低"),
    MEDIUM(2, "中"),
    HIGH(3, "高"),
    URGENT(4, "紧急");

    private final int code;
    private final String desc;

    CrisisLevel(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
