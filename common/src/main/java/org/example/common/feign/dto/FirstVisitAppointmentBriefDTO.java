package org.example.common.feign.dto;

import lombok.Data;

/**
 * 初访预约简要信息（服务间校验用）
 */
@Data
public class FirstVisitAppointmentBriefDTO {

    private Long id;
    private Long studentId;
    private Long visitorId;
    private Integer status;
}
