package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.enums.ConsultationStatus;
import org.example.common.exception.BusinessException;
import org.example.consultation.dto.ArrangeDTO;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.mapper.ConsultationAppointmentMapper;
import org.example.consultation.service.ConsultationAppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConsultationAppointmentServiceImpl extends ServiceImpl<ConsultationAppointmentMapper, ConsultationAppointment>
        implements ConsultationAppointmentService {

    @Override
    @Transactional
    public ConsultationAppointment arrange(ArrangeDTO dto) {
        // 检查咨询师在该时段是否已被占用
        long conflict = lambdaQuery()
                .eq(ConsultationAppointment::getCounselorId, dto.getCounselorId())
                .eq(ConsultationAppointment::getTimeSlotId, dto.getTimeSlotId())
                .eq(ConsultationAppointment::getStartDate, dto.getStartDate())
                .eq(ConsultationAppointment::getStatus, 1) // 进行中
                .count();
        if (conflict > 0) {
            throw new BusinessException("该咨询师在当前时段已被占用，请选择其他时段或咨询师");
        }

        int weeks = dto.getOccupiedWeeks() != null ? dto.getOccupiedWeeks() : 8;

        ConsultationAppointment app = new ConsultationAppointment();
        app.setStudentId(dto.getStudentId());
        app.setFirstVisitResultId(dto.getResultId());
        app.setCounselorId(dto.getCounselorId());
        app.setStartDate(dto.getStartDate());
        app.setTimeSlotId(dto.getTimeSlotId());
        app.setDayOfWeek(dto.getStartDate().getDayOfWeek().getValue());
        app.setLocation(dto.getLocation());
        app.setOccupiedWeeks(weeks);
        app.setRemainingWeeks(weeks);
        app.setStatus(1); // 进行中
        app.setNotifyTime(LocalDateTime.now());
        save(app);

        // TODO: 发送短信通知学生
        return app;
    }

    @Override
    @Transactional
    public void closeEarly(Long appointmentId) {
        ConsultationAppointment app = getById(appointmentId);
        if (app == null) throw new BusinessException("咨询安排不存在");
        app.setRemainingWeeks(0);
        app.setStatus(2); // 结案
        updateById(app);
    }

    @Override
    public Page<ConsultationAppointment> listAll(Integer page, Integer size) {
        return lambdaQuery()
                .orderByDesc(ConsultationAppointment::getCreateTime)
                .page(new Page<>(page, size));
    }

    @Override
    @Transactional
    public void updateArrangement(Long id, ArrangeDTO dto) {
        ConsultationAppointment app = getById(id);
        if (app == null) throw new BusinessException("咨询安排不存在");
        if (dto.getCounselorId() != null) app.setCounselorId(dto.getCounselorId());
        if (dto.getStartDate() != null) app.setStartDate(dto.getStartDate());
        if (dto.getTimeSlotId() != null) app.setTimeSlotId(dto.getTimeSlotId());
        if (dto.getLocation() != null) app.setLocation(dto.getLocation());
        updateById(app);
    }
}
