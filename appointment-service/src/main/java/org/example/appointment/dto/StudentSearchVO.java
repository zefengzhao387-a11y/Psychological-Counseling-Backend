package org.example.appointment.dto;

import lombok.Data;

/**
 * 学生搜索 VO（按学号/姓名搜索学生及其登记表）
 */
@Data
public class StudentSearchVO {

    /** 学生用户ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 学号 */
    private String studentNo;

    /** 院系 */
    private String department;

    /** 联系电话 */
    private String phone;

    /** 最新登记表ID（如果有） */
    private Long formId;

    /** 是否有登记表 */
    private Boolean hasForm;
}
