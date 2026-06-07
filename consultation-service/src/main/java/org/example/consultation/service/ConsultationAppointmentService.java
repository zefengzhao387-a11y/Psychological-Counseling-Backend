package org.example.consultation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.consultation.dto.ArrangeDTO;
import org.example.consultation.entity.ConsultationAppointment;

public interface ConsultationAppointmentService extends IService<ConsultationAppointment> {

    /**
     * 心理助理安排咨询：
     * - 匹配咨询师空闲时间
     * - 默认占用 8 周同一时间段
     * - 短信通知学生
     */
    ConsultationAppointment arrange(ArrangeDTO dto);

    /** 提前结案，释放剩余时段 */
    void closeEarly(Long appointmentId);

    /** 咨询安排列表（可按咨询师筛选） */
    Page<ConsultationAppointment> listAll(Integer page, Integer size, Long counselorId);

    /** 改约咨询安排 */
    void updateArrangement(Long id, ArrangeDTO dto);
}
