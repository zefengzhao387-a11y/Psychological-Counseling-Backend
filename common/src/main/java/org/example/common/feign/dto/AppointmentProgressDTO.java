package org.example.common.feign.dto;

import lombok.Data;

/**
 * 初访预约关联的全流程进度（管理员展示）
 */
@Data
public class AppointmentProgressDTO {

    private Long appointmentId;
    /** 咨询阶段：— / 待评估 / 无需咨询 / 转介送诊 / 待安排 / 咨询中 / 已结案 / 已脱落 */
    private String consultationProgress;
    /** 结案阶段：— / 未开始 / 待写报告 / 草稿 / 已提交 / 已审核 / 已驳回 */
    private String closingProgress;
}
