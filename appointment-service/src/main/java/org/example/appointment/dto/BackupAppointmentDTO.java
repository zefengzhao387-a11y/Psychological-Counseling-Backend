package org.example.appointment.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 补录备班 DTO（管理员为未线上预约的来访学生补录预约记录）
 */
@Data
public class BackupAppointmentDTO {

    /** 搜索关键词（学号或姓名，模糊匹配），与 studentId 二选一 */
    private String keyword;

    /** 学生ID（已知学生时直接传） */
    private Long studentId;

    /** 学生姓名（补录时如无登记表，直接填写） */
    private String studentName;

    /** 学号（补录时如无登记表，直接填写） */
    private String studentNo;

    /** 联系电话（补录时如无登记表，直接填写） */
    private String phone;

    /** 初访员ID（不传则自动匹配空闲初访员） */
    private Long visitorId;

    /** 预约日期 */
    private LocalDate appointmentDate;

    /** 时间段ID */
    private Long timeSlotId;

    /** 咨询地点 */
    private String location;

    /** 备注 */
    private String remark;
}
