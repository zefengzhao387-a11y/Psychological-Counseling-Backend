package org.example.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

/**
 * 咨询师/初访员信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CounselorInfo extends BaseEntity {

    /** 关联 sys_user.id */
    private Long userId;

    /** 姓名 */
    private String name;

    /** 性别 */
    private String gender;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 类型：1初访员 2咨询师 */
    private Integer type;

    /** 专业资质 */
    private String qualification;

    /** 擅长领域（逗号分隔） */
    private String specialty;

    /** 状态：1在职 2离职 */
    private Integer status;
}
