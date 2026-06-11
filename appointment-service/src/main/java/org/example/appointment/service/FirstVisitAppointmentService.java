package org.example.appointment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.common.feign.dto.FirstVisitAppointmentBriefDTO;
import org.example.appointment.dto.*;
import org.example.appointment.entity.FirstVisitAppointment;

import java.util.List;

public interface FirstVisitAppointmentService extends IService<FirstVisitAppointment> {

    /** 学生提交初访预约 */
    FirstVisitAppointment submit(Long studentId, Long formId, Long dutyScheduleId, java.time.LocalDate date, Long timeSlotId);

    /** 学生撤销预约 */
    void cancel(Long appointmentId, Long studentId);

    /**
     * 管理员审核列表（计分报警排序）：
     * - is_urgent=true 的排在前面（高亮展示）
     * - is_priority=true 的紧随其后
     * - 其余按 create_time 升序（先排队的先审核）
     */
    Page<AppointmentVO> reviewList(Integer page, Integer size, Integer status);

    /** 管理员审核（通过/拒绝） */
    void review(ReviewDTO dto, Long reviewerId);

    /** 优先排队：标记/取消优先 */
    void togglePriority(Long appointmentId);

    /** 改约 */
    void reschedule(RescheduleDTO dto);

    /**
     * 管理员按学号/姓名新增预约并自动匹配空闲初访员
     * - 支持 keyword 搜索匹配学生
     * - 若未指定初访员，自动匹配该时段预约数最少的空闲初访员
     */
    void addAppointment(AppointmentAddDTO dto, Long adminId);

    /** 按关键词搜索学生（学号/姓名模糊匹配） */
    List<StudentSearchVO> searchStudent(String keyword);

    /**
     * 管理员补录备班（为未线上预约的来访学生手动补录）
     * - 支持 keyword 查找已有学生或直接填写学生信息
     * - 自动匹配空闲初访员（优先指定初访员）
     * - 若该时段无值班安排则自动创建备班记录
     */
    void backupAppointment(BackupAppointmentDTO dto, Long adminId);

    /** 获取学生自己的预约列表 */
    Page<AppointmentVO> myAppointments(Integer page, Integer size, Long studentId);

    /** 获取某学生今日是否有已通过预约 */
    AppointmentVO getTodayAppointment(Long studentId, java.time.LocalDate date);

    /** 初访员查看分配给自己的已通过预约 */
    List<AppointmentVO> listForVisitor(Long visitorId);

    /** 初访完成后标记预约为已完成（consultation-service 调用） */
    void markCompleted(Long appointmentId);

    /** 服务间：预约简要信息 */
    FirstVisitAppointmentBriefDTO getBrief(Long appointmentId);
}
