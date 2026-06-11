package org.example.appointment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.appointment.dto.AppointmentAddDTO;
import org.example.appointment.entity.DutySchedule;
import org.example.appointment.mapper.DutyScheduleMapper;
import org.example.appointment.service.DutyScheduleService;
import org.example.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DutyScheduleServiceImpl extends ServiceImpl<DutyScheduleMapper, DutySchedule> implements DutyScheduleService {

    @Override
    @Transactional
    public void batchCreate(Long counselorId, Integer counselorType, LocalDate startDate, LocalDate endDate,
                            List<Long> timeSlotIds, Integer maxAppointments) {
        List<DutySchedule> list = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            for (Long slotId : timeSlotIds) {
                DutySchedule ds = new DutySchedule();
                ds.setCounselorId(counselorId);
                ds.setCounselorType(counselorType);
                ds.setDutyDate(date);
                ds.setTimeSlotId(slotId);
                ds.setMaxAppointments(maxAppointments);
                ds.setBookedCount(0);
                list.add(ds);
            }
            date = date.plusDays(1);
        }
        saveBatch(list);
    }

    @Override
    public List<DutySchedule> listAvailable(LocalDate date, Long timeSlotId, Integer counselorType) {
        var query = lambdaQuery()
                .eq(DutySchedule::getDutyDate, date)
                .eq(DutySchedule::getTimeSlotId, timeSlotId)
                .apply("booked_count < max_appointments");
        if (counselorType != null) {
            query.eq(DutySchedule::getCounselorType, counselorType);
        }
        return query.list();
    }

    @Override
    public List<DutySchedule> listByDate(LocalDate date) {
        return lambdaQuery().eq(DutySchedule::getDutyDate, date).list();
    }

    @Override
    public void incrementBooked(Long scheduleId) {
        DutySchedule ds = getById(scheduleId);
        if (ds != null && ds.getBookedCount() < ds.getMaxAppointments()) {
            ds.setBookedCount(ds.getBookedCount() + 1);
            updateById(ds);
        }
    }

    @Override
    public void decrementBooked(Long scheduleId) {
        DutySchedule ds = getById(scheduleId);
        if (ds != null && ds.getBookedCount() > 0) {
            ds.setBookedCount(ds.getBookedCount() - 1);
            updateById(ds);
        }
    }

    @Override
    public Long matchVisitor(AppointmentAddDTO dto) {
        List<DutySchedule> available = lambdaQuery()
                .eq(DutySchedule::getDutyDate, dto.getAppointmentDate())
                .eq(DutySchedule::getTimeSlotId, dto.getTimeSlotId())
                .eq(DutySchedule::getCounselorType, 1) // 初访员
                .apply("booked_count < max_appointments")
                .list();
        if (available.isEmpty()) {
            throw new BusinessException("该时段暂无空闲初访员");
        }
        // 优先匹配 dto 指定的老师，否则选预约数最少的
        if (dto.getVisitorId() != null) {
            for (DutySchedule ds : available) {
                if (ds.getCounselorId().equals(dto.getVisitorId())) {
                    return ds.getId();
                }
            }
        }
        available.sort((a, b) -> a.getBookedCount() - b.getBookedCount());
        return available.get(0).getId();
    }

    @Override
    public Long matchAvailableVisitor(LocalDate date, Long timeSlotId, Long preferVisitorId) {
        List<DutySchedule> available = lambdaQuery()
                .eq(DutySchedule::getDutyDate, date)
                .eq(DutySchedule::getTimeSlotId, timeSlotId)
                .eq(DutySchedule::getCounselorType, 1)
                .apply("booked_count < max_appointments")
                .list();
        if (available.isEmpty()) {
            throw new BusinessException("该时段暂无空闲初访员");
        }
        if (preferVisitorId != null) {
            for (DutySchedule ds : available) {
                if (ds.getCounselorId().equals(preferVisitorId)) {
                    return ds.getId();
                }
            }
        }
        available.sort((a, b) -> a.getBookedCount() - b.getBookedCount());
        return available.get(0).getId();
    }

    @Override
    @Transactional
    public Long findOrCreateBackupSlot(Long visitorId, LocalDate date, Long timeSlotId) {
        // 先查找已有值班安排
        DutySchedule existing = lambdaQuery()
                .eq(DutySchedule::getCounselorId, visitorId)
                .eq(DutySchedule::getDutyDate, date)
                .eq(DutySchedule::getTimeSlotId, timeSlotId)
                .one();
        if (existing != null) {
            return existing.getId();
        }
        // 创建备班记录
        DutySchedule backup = new DutySchedule();
        backup.setCounselorId(visitorId);
        backup.setCounselorType(1);
        backup.setDutyDate(date);
        backup.setTimeSlotId(timeSlotId);
        backup.setMaxAppointments(4);
        backup.setBookedCount(0);
        save(backup);
        return backup.getId();
    }
}
