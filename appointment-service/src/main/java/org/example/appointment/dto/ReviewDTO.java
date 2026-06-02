package org.example.appointment.dto;

import lombok.Data;

/**
 * 审核操作 DTO
 */
@Data
public class ReviewDTO {

    /** 预约ID */
    private Long appointmentId;

    /** 审核结果：2通过 3拒绝 */
    private Integer status;

    /** 分配的初访员ID（通过时必填） */
    private Long visitorId;

    /** 咨询地点（通过时必填） */
    private String location;

    /** 审核备注 */
    private String remark;
}
