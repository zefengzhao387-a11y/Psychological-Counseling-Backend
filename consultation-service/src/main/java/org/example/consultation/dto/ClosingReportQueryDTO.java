package org.example.consultation.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 结案报告分页查询 DTO
 */
@Data
public class ClosingReportQueryDTO {

    /** 咨询安排ID */
    private Long appointmentId;

    /** 学号（模糊查询） */
    private String studentNo;

    /** 学生姓名（模糊查询） */
    private String studentName;

    /** 性别 */
    private String gender;

    /** 年级（模糊查询） */
    private String studentGrade;

    /** 咨询师ID */
    private Long counselorId;

    /** 问题类型 */
    private Integer problemType;

    /** 结案原因 */
    private String closingReason;

    /** 咨询方式 */
    private String consultationMethod;

    /** 状态 */
    private String status;

    /** 风险评估等级 */
    private String riskLevel;

    /** 院系 */
    private String department;

    /** 首次咨询日期起始 */
    private LocalDateTime firstConsultationStart;

    /** 首次咨询日期结束 */
    private LocalDateTime firstConsultationEnd;

    /** 结案日期起始 */
    private LocalDateTime closingDateStart;

    /** 结案日期结束 */
    private LocalDateTime closingDateEnd;

    /** 当前页码（默认1） */
    private Long page = 1L;

    /** 每页条数（默认10） */
    private Long size = 10L;
}
