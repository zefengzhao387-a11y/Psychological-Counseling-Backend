package org.example.appointment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.appointment.dto.AppointmentAddDTO;
import org.example.appointment.entity.DutySchedule;

import java.time.LocalDate;
import java.util.List;

public interface DutyScheduleService extends IService<DutySchedule> {

    /** 批量创建值班安排 */
    void batchCreate(Long counselorId, Integer counselorType, LocalDate startDate, LocalDate endDate,
                     List<Long> timeSlotIds, Integer maxAppointments);

    /** 查询某日某时段所有空闲老师 */
    List<DutySchedule> listAvailable(LocalDate date, Long timeSlotId);

    /** 根据日期查询值班 */
    List<DutySchedule> listByDate(LocalDate date);

    /** 预约数+1 */
    void incrementBooked(Long scheduleId);

    /** 预约数-1 */
    void decrementBooked(Long scheduleId);

    /** 匹配空闲初访员 */
    Long matchVisitor(AppointmentAddDTO dto);
}
