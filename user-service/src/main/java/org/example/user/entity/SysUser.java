package org.example.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    /** 学号/工号 */
    private String userNo;

    /** 姓名 */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 手机号 */
    private String phone;

    /** 性别 */
    private String gender;

    /** 院系 */
    private String department;

    /** 角色：1学生 2初访员 3心理助理 4咨询师 5中心管理员 */
    private Integer roleCode;
}
