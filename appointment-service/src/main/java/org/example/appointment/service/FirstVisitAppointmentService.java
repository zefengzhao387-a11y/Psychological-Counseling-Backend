package org.example.appointment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.appointment.dto.AppointmentAddDTO;
import org.example.appointment.dto.AppointmentVO;
import org.example.appointment.dto.RescheduleDTO;
import org.example.appointment.dto.ReviewDTO;
import org.example.appointment.entity.FirstVisitAppointment;

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

    /** 管理员新增预约 */
    void addAppointment(AppointmentAddDTO dto, Long adminId);

    /** 获取学生自己的预约列表 */
    Page<AppointmentVO> myAppointments(Integer page, Integer size, Long studentId);

    /** 获取某学生今日是否有已通过预约 */
    AppointmentVO getTodayAppointment(Long studentId, java.time.LocalDate date);
}
