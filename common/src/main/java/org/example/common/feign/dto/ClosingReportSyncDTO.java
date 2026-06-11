package org.example.common.feign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结案报告同步 DTO（consultation → statistics）
 */
@Data
public class ClosingReportSyncDTO {

    private Long id;
    private Long appointmentId;
    private Long counselorId;

    private String studentNo;
    private String studentName;
    private String gender;
    private String studentGrade;
    private String department;
    private String studentMajor;
    private String phone;
    private String studentEmail;

    private Integer problemType;
    private String consultationMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstConsultationDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closingDate;

    private Integer totalSessions;
    private BigDecimal totalHours;

    private String closingReason;
    private String closingReasonDetail;
    private String caseSummary;
    private String selfEvaluation;
    private String counselingOutcome;
    private String followUpPlan;
    private String referralInfo;

    private String riskLevel;
    private String riskNote;
    private String status;

    private Long reviewerId;
    private String reviewerName;
    private String reviewComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewDate;

    private String filePath;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
