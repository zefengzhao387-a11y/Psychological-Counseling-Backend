package org.example.appointment.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约记录 VO（列表展示用）
 */
@Data
public class AppointmentVO {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long formId;
    private Integer totalScore;
    private Integer isUrgent;
    private Integer isPriority;
    private Long visitorId;
    private String visitorName;
    private LocalDate appointmentDate;
    private String timeSlotName;
    private String location;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;
}
