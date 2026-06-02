package org.example.appointment.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

import java.time.LocalTime;

/**
 * 时间段配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TimeConfig extends BaseEntity {

    /** 时间段名称，如"08:00-08:50" */
    private String slotName;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 来访间隔（分钟） */
    private Integer intervalMinutes;
}
