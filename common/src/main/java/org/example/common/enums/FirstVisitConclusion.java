package org.example.common.enums;

import lombok.Getter;

/**
 * 初访结论枚举
 */
@Getter
public enum FirstVisitConclusion {

    NO_NEED(1, "无需咨询"),
    ARRANGE(2, "安排咨询"),
    REFER(3, "转介送诊");

    private final int code;
    private final String desc;

    FirstVisitConclusion(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
