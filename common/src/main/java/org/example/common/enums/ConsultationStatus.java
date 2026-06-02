package org.example.common.enums;

import lombok.Getter;

/**
 * 咨询状态枚举
 */
@Getter
public enum ConsultationStatus {

    COMPLETED(1, "完成咨询"),
    ABSENT(2, "旷约"),
    LEAVE(3, "请假"),
    DROPOUT(4, "脱落"),
    CLOSED(5, "结案");

    private final int code;
    private final String desc;

    ConsultationStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
