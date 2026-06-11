package org.example.consultation.dto;

import lombok.Data;

/**
 * 管理员审核结案报告
 */
@Data
public class ClosingReportReviewDTO {

    /** 已审核 / 已驳回 */
    private String status;

    private String reviewComment;
}
