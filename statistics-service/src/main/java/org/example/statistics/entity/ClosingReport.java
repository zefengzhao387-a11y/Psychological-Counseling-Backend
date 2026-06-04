package org.example.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结案报告实体类（统计分析服务只读引用）
 */
@Data
@TableName("closing_report")
public class ClosingReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;
    private Long counselorId;

    /** 学号 */
    private String studentNo;
    /** 学生姓名 */
    private String studentName;
    /** 性别 */
    private String gender;
    /** 年级 */
    private String studentGrade;
    /** 院系 */
    private String department;
    /** 专业 */
    private String studentMajor;
    /** 电话 */
    private String phone;
    /** 邮箱 */
    private String studentEmail;

    /** 问题类型 */
    private Integer problemType;
    /** 咨询方式 */
    private String consultationMethod;
    /** 首次咨询日期 */
    private LocalDateTime firstConsultationDate;
    /** 结案日期 */
    private LocalDateTime closingDate;

    /** 总咨询次数 */
    private Integer totalSessions;
    /** 总咨询时长 */
    private BigDecimal totalHours;

    /** 结案原因 */
    private String closingReason;
    /** 结案原因说明 */
    private String closingReasonDetail;
    /** 个案摘要 */
    private String caseSummary;
    /** 咨询效果自评 */
    private String selfEvaluation;
    /** 咨询效果评估 */
    private String counselingOutcome;
    /** 后续跟进计划 */
    private String followUpPlan;
    /** 转介信息 */
    private String referralInfo;

    /** 风险等级 */
    private String riskLevel;
    /** 风险备注 */
    private String riskNote;

    /** 状态 */
    private String status;
    /** 审核人ID */
    private Long reviewerId;
    /** 审核人姓名 */
    private String reviewerName;
    /** 审核意见 */
    private String reviewComment;
    /** 审核日期 */
    private LocalDateTime reviewDate;

    /** Word文件路径 */
    private String filePath;

    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
