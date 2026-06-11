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

    /** 查询某日某时段所有空闲老师（counselorType: 1初访员 2咨询师，默认不限） */
    List<DutySchedule> listAvailable(LocalDate date, Long timeSlotId, Integer counselorType);

    /** 根据日期查询值班 */
    List<DutySchedule> listByDate(LocalDate date);

    /** 预约数+1 */
    void incrementBooked(Long scheduleId);

    /** 预约数-1 */
    void decrementBooked(Long scheduleId);

    /** 匹配空闲初访员，返回 dutyScheduleId */
    Long matchVisitor(AppointmentAddDTO dto);

    /**
     * 自动匹配某日某时段的空闲初访员（补录备班用）
     * @param date        预约日期
     * @param timeSlotId  时间段ID
     * @param preferVisitorId 优先匹配的初访员ID（可为null）
     * @return 匹配到的值班安排ID
     */
    Long matchAvailableVisitor(LocalDate date, Long timeSlotId, Long preferVisitorId);

    /**
     * 查找或创建备班值班记录（补录备班时若该时段无值班安排则自动创建）
     * @param visitorId   初访员ID
     * @param date        日期
     * @param timeSlotId  时间段ID
     * @return 值班安排ID
     */
    Long findOrCreateBackupSlot(Long visitorId, LocalDate date, Long timeSlotId);
}
