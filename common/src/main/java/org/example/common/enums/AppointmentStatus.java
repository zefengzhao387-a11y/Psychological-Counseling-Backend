package org.example.common.enums;

import lombok.Getter;

/**
 * 预约状态枚举
 */
@Getter
public enum AppointmentStatus {

    PENDING(1, "待审核"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已拒绝"),
    CANCELLED(4, "已撤销");

    private final int code;
    private final String desc;

    AppointmentStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
