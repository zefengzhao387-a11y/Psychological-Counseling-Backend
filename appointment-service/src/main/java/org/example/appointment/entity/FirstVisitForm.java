package org.example.appointment.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * 首访登记表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FirstVisitForm extends BaseEntity {

    /** 学生用户ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 学号 */
    private String studentNo;

    /** 性别 */
    private String gender;

    /** 院系 */
    private String department;

    /** 联系电话 */
    private String phone;

    /** 问卷答案 JSON */
    private String questionnaire;

    /** 问卷总分 */
    private Integer totalScore;

    /** 是否紧急（计分报警） */
    private Integer isUrgent;

    /** 是否已阅读知情同意书 */
    private Integer hasReadConsent;

    /** 同意时间 */
    private LocalDateTime consentTime;

    /** 电子签名（姓名） */
    private String consentSignature;
}
