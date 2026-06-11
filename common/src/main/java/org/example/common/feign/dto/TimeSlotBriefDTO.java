package org.example.common.feign.dto;

import lombok.Data;

/**
 * 时间段简要信息（跨服务 Feign 调用）
 */
@Data
public class TimeSlotBriefDTO {

    private Long id;
    private String slotName;
}
