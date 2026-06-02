package org.example.common.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {

    STUDENT(1, "学生"),
    FIRST_VISITOR(2, "初访员"),
    PSYCH_ASSISTANT(3, "心理助理"),
    COUNSELOR(4, "咨询师"),
    CENTER_ADMIN(5, "中心管理员");

    private final int code;
    private final String desc;

    UserRole(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
