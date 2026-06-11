package org.example.consultation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 结案报告新增/修改 DTO
 */
@Data
public class ClosingReportSaveDTO {

    /** 咨询安排ID */
    private Long appointmentId;

    // ==================== 学生信息 ====================

    /** 来访者学号 */
    private String studentNo;

    /** 来访者姓名 */
    private String studentName;

    /** 来访者性别 */
    private String gender;

    /** 年级 */
    private String studentGrade;

    /** 来访者院系 */
    private String department;

    /** 专业 */
    private String studentMajor;

    /** 来访者联系电话 */
    private String phone;

    /** 电子邮箱 */
    private String studentEmail;

    // ==================== 咨询基本信息 ====================

    /** 问题类型 */
    private Integer problemType;

    /** 咨询方式 */
    private String consultationMethod;

    /** 首次咨询日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstConsultationDate;

    /** 结案日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closingDate;

    // ==================== 咨询统计 ====================

    /** 咨询总次数 */
    private Integer totalSessions;

    /** 总咨询时长（小时） */
    private BigDecimal totalHours;

    // ==================== 结案核心内容 ====================

    /** 结案原因 */
    private String closingReason;

    /** 结案原因详细说明 */
    private String closingReasonDetail;

    /** 个案摘要 */
    private String caseSummary;

    /** 咨询效果自评 */
    private String selfEvaluation;

    /** 咨询效果评估（咨询师） */
    private String counselingOutcome;

    /** 后续跟进计划 */
    private String followUpPlan;

    /** 转介信息 */
    private String referralInfo;

    // ==================== 风险评估 ====================

    /** 风险评估等级 */
    private String riskLevel;

    /** 风险备注 */
    private String riskNote;

    // ==================== 状态 ====================

    /** 状态 */
    private String status;
}
