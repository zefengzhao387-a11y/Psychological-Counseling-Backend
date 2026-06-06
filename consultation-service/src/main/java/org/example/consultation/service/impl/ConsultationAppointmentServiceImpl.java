package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.BusinessException;
import org.example.consultation.dto.ArrangeDTO;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.entity.FirstVisitResult;
import org.example.consultation.mapper.ConsultationAppointmentMapper;
import org.example.consultation.service.ConsultationAppointmentService;
import org.example.consultation.service.FirstVisitResultService;
import org.example.consultation.support.NotificationSupport;
import org.example.consultation.util.WeeklySlotUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ConsultationAppointmentServiceImpl extends ServiceImpl<ConsultationAppointmentMapper, ConsultationAppointment>
        implements ConsultationAppointmentService {

    private static final int DEFAULT_WEEKS = 8;

    private final NotificationSupport notificationSupport;
    private final FirstVisitResultService firstVisitResultService;

    public ConsultationAppointmentServiceImpl(NotificationSupport notificationSupport,
                                              FirstVisitResultService firstVisitResultService) {
        this.notificationSupport = notificationSupport;
        this.firstVisitResultService = firstVisitResultService;
    }

    @Override
    @Transactional
    public ConsultationAppointment arrange(ArrangeDTO dto) {
        int weeks = dto.getOccupiedWeeks() != null ? dto.getOccupiedWeeks() : DEFAULT_WEEKS;
        assertNoWeeklyConflict(dto.getCounselorId(), dto.getTimeSlotId(), dto.getStartDate(), weeks, null);

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
        app.setStatus(1);
        app.setNotifyTime(LocalDateTime.now());
        save(app);

        if (dto.getResultId() != null) {
            FirstVisitResult result = firstVisitResultService.getById(dto.getResultId());
            if (result != null) {
                result.setAssistantStatus(1);
                firstVisitResultService.updateById(result);
            }
        }

        notificationSupport.sendSms(
                null,
                String.format("您的心理咨询已安排：%s 起连续 %d 周，地点 %s。",
                        dto.getStartDate(), weeks, dto.getLocation()),
                "CONSULTATION_ARRANGE");
        return app;
    }

    @Override
    @Transactional
    public void closeEarly(Long appointmentId) {
        ConsultationAppointment app = getById(appointmentId);
        if (app == null) throw new BusinessException("咨询安排不存在");
        app.setRemainingWeeks(0);
        app.setStatus(2);
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

        Long counselorId = dto.getCounselorId() != null ? dto.getCounselorId() : app.getCounselorId();
        Long timeSlotId = dto.getTimeSlotId() != null ? dto.getTimeSlotId() : app.getTimeSlotId();
        LocalDate startDate = dto.getStartDate() != null ? dto.getStartDate() : app.getStartDate();
        int weeks = app.getRemainingWeeks() != null ? app.getRemainingWeeks() : DEFAULT_WEEKS;

        assertNoWeeklyConflict(counselorId, timeSlotId, startDate, weeks, id);

        if (dto.getCounselorId() != null) app.setCounselorId(dto.getCounselorId());
        if (dto.getStartDate() != null) {
            app.setStartDate(dto.getStartDate());
            app.setDayOfWeek(dto.getStartDate().getDayOfWeek().getValue());
        }
        if (dto.getTimeSlotId() != null) app.setTimeSlotId(dto.getTimeSlotId());
        if (dto.getLocation() != null) app.setLocation(dto.getLocation());
        updateById(app);
    }

    private void assertNoWeeklyConflict(Long counselorId, Long timeSlotId, LocalDate startDate,
                                        int weeks, Long excludeId) {
        int dayOfWeek = startDate.getDayOfWeek().getValue();
        Set<LocalDate> newSlots = WeeklySlotUtil.slotsForNewAppointment(startDate, weeks);

        var query = lambdaQuery()
                .eq(ConsultationAppointment::getCounselorId, counselorId)
                .eq(ConsultationAppointment::getTimeSlotId, timeSlotId)
                .eq(ConsultationAppointment::getDayOfWeek, dayOfWeek)
                .eq(ConsultationAppointment::getStatus, 1);
        if (excludeId != null) {
            query.ne(ConsultationAppointment::getId, excludeId);
        }

        List<ConsultationAppointment> existingList = query.list();
        for (ConsultationAppointment existing : existingList) {
            Set<LocalDate> existingSlots = WeeklySlotUtil.slotsForExistingAppointment(existing);
            if (WeeklySlotUtil.hasOverlap(newSlots, existingSlots)) {
                throw new BusinessException(
                        "该咨询师在未来 " + weeks + " 周的此时段已被占用，请更换咨询师、日期或时间段");
            }
        }
    }
}
