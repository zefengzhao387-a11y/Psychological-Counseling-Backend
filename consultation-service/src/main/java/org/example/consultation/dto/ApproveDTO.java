package org.example.consultation.dto;

import lombok.Data;

/**
 * 管理员 — 审批追加申请 DTO
 */
@Data
public class ApproveDTO {

    private Long extensionId;
    /** 2通过 3拒绝 */
    private Integer status;
    private String remark;
}
