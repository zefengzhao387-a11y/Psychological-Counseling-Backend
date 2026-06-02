package org.example.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.appointment.dto.AppointmentAddDTO;
import org.example.appointment.dto.AppointmentVO;
import org.example.appointment.dto.RescheduleDTO;
import org.example.appointment.dto.ReviewDTO;
import org.example.appointment.entity.DutySchedule;
import org.example.appointment.entity.FirstVisitAppointment;
import org.example.appointment.entity.FirstVisitForm;
import org.example.appointment.entity.TimeConfig;
import org.example.appointment.mapper.FirstVisitAppointmentMapper;
import org.example.appointment.service.DutyScheduleService;
import org.example.appointment.service.FirstVisitAppointmentService;
import org.example.appointment.service.FirstVisitFormService;
import org.example.appointment.service.TimeConfigService;
import org.example.common.enums.AppointmentStatus;
import org.example.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class FirstVisitAppointmentServiceImpl extends ServiceImpl<FirstVisitAppointmentMapper, FirstVisitAppointment>
        implements FirstVisitAppointmentService {

    private final FirstVisitFormService formService;
    private final DutyScheduleService dutyScheduleService;
    private final TimeConfigService timeConfigService;

    public FirstVisitAppointmentServiceImpl(FirstVisitFormService formService,
                                             DutyScheduleService dutyScheduleService,
                                             TimeConfigService timeConfigService) {
        this.formService = formService;
        this.dutyScheduleService = dutyScheduleService;
        this.timeConfigService = timeConfigService;
    }

    /** 计分报警阈值 */
    private static final int URGENT_SCORE_THRESHOLD = 60;

    @Override
    @Transactional
    public FirstVisitAppointment submit(Long studentId, Long formId, Long dutyScheduleId,
                                         LocalDate date, Long timeSlotId) {
        DutySchedule ds = dutyScheduleService.getById(dutyScheduleId);
        if (ds == null || ds.getBookedCount() >= ds.getMaxAppointments()) {
            throw new BusinessException("该时段已约满，请选择其他时间");
        }

        long count = lambdaQuery()
                .eq(FirstVisitAppointment::getStudentId, studentId)
                .in(FirstVisitAppointment::getStatus, 1, 2)
                .count();
        if (count > 0) {
            throw new BusinessException("您已有进行中的预约，请勿重复预约");
        }

        FirstVisitForm form = formService.getById(formId);
        boolean isUrgent = form.getTotalScore() != null && form.getTotalScore() >= URGENT_SCORE_THRESHOLD;

        FirstVisitAppointment appointment = new FirstVisitAppointment();
        appointment.setStudentId(studentId);
        appointment.setFormId(formId);
        appointment.setDutyScheduleId(dutyScheduleId);
        appointment.setVisitorId(ds.getCounselorId());
        appointment.setAppointmentDate(date);
        appointment.setTimeSlotId(timeSlotId);
        appointment.setStatus(AppointmentStatus.PENDING.getCode());
        appointment.setIsPriority(isUrgent ? 1 : 0);
        save(appointment);

        dutyScheduleService.incrementBooked(dutyScheduleId);
        return appointment;
    }

    @Override
    @Transactional
    public void cancel(Long appointmentId, Long studentId) {
        FirstVisitAppointment app = getById(appointmentId);
        if (app == null || !app.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作此预约");
        }
        if (app.getStatus() != AppointmentStatus.PENDING.getCode()) {
            throw new BusinessException("仅待审核状态可撤销");
        }
        app.setStatus(AppointmentStatus.CANCELLED.getCode());
        updateById(app);

        if (app.getDutyScheduleId() != null) {
            dutyScheduleService.decrementBooked(app.getDutyScheduleId());
        }
    }

    @Override
    public Page<AppointmentVO> reviewList(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<FirstVisitAppointment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(FirstVisitAppointment::getStatus, status);
        }
        wrapper.orderByDesc(FirstVisitAppointment::getIsPriority)
               .orderByAsc(FirstVisitAppointment::getCreateTime);
        Page<FirstVisitAppointment> pageResult = page(new Page<>(page, size), wrapper);
        Page<AppointmentVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream().map(this::toVO).collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public void review(ReviewDTO dto, Long reviewerId) {
        FirstVisitAppointment app = getById(dto.getAppointmentId());
        if (app == null || app.getStatus() != AppointmentStatus.PENDING.getCode()) {
            throw new BusinessException("该预约不在待审核状态");
        }

        app.setReviewerId(reviewerId);
        app.setReviewTime(LocalDateTime.now());

        if (dto.getStatus() == AppointmentStatus.APPROVED.getCode()) {
            if (dto.getVisitorId() == null) {
                throw new BusinessException("请选择初访员");
            }
            app.setVisitorId(dto.getVisitorId());
            app.setLocation(dto.getLocation());
            app.setStatus(AppointmentStatus.APPROVED.getCode());
        } else if (dto.getStatus() == AppointmentStatus.REJECTED.getCode()) {
            app.setStatus(AppointmentStatus.REJECTED.getCode());
            app.setReviewRemark(dto.getRemark());
            if (app.getDutyScheduleId() != null) {
                dutyScheduleService.decrementBooked(app.getDutyScheduleId());
            }
        }
        updateById(app);
    }

    @Override
    @Transactional
    public void togglePriority(Long appointmentId) {
        FirstVisitAppointment app = getById(appointmentId);
        if (app == null) {
            throw new BusinessException("预约不存在");
        }
        app.setIsPriority(app.getIsPriority() == 1 ? 0 : 1);
        updateById(app);
    }

    @Override
    @Transactional
    public void reschedule(RescheduleDTO dto) {
        FirstVisitAppointment app = getById(dto.getAppointmentId());
        if (app == null) {
            throw new BusinessException("预约不存在");
        }
        if (dto.getVisitorId() != null) app.setVisitorId(dto.getVisitorId());
        if (dto.getAppointmentDate() != null) app.setAppointmentDate(dto.getAppointmentDate());
        if (dto.getTimeSlotId() != null) app.setTimeSlotId(dto.getTimeSlotId());
        if (dto.getLocation() != null) app.setLocation(dto.getLocation());
        updateById(app);
    }

    @Override
    @Transactional
    public void addAppointment(AppointmentAddDTO dto, Long adminId) {
        FirstVisitAppointment app = new FirstVisitAppointment();
        app.setStudentId(dto.getStudentId());
        app.setFormId(dto.getFormId());
        app.setVisitorId(dto.getVisitorId());
        app.setAppointmentDate(dto.getAppointmentDate());
        app.setTimeSlotId(dto.getTimeSlotId());
        app.setLocation(dto.getLocation());
        app.setStatus(AppointmentStatus.APPROVED.getCode());
        app.setReviewerId(adminId);
        app.setReviewTime(LocalDateTime.now());
        save(app);
    }

    @Override
    public Page<AppointmentVO> myAppointments(Integer page, Integer size, Long studentId) {
        LambdaQueryWrapper<FirstVisitAppointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FirstVisitAppointment::getStudentId, studentId)
               .orderByDesc(FirstVisitAppointment::getCreateTime);
        Page<FirstVisitAppointment> pageResult = page(new Page<>(page, size), wrapper);
        Page<AppointmentVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream().map(this::toVO).collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public AppointmentVO getTodayAppointment(Long studentId, LocalDate date) {
        FirstVisitAppointment app = lambdaQuery()
                .eq(FirstVisitAppointment::getStudentId, studentId)
                .eq(FirstVisitAppointment::getAppointmentDate, date)
                .eq(FirstVisitAppointment::getStatus, AppointmentStatus.APPROVED.getCode())
                .one();
        return app != null ? toVO(app) : null;
    }

    private AppointmentVO toVO(FirstVisitAppointment app) {
        AppointmentVO vo = new AppointmentVO();
        vo.setId(app.getId());
        vo.setStudentId(app.getStudentId());
        vo.setFormId(app.getFormId());
        vo.setVisitorId(app.getVisitorId());
        vo.setAppointmentDate(app.getAppointmentDate());
        vo.setLocation(app.getLocation());
        vo.setStatus(app.getStatus());
        vo.setIsPriority(app.getIsPriority());
        vo.setCreateTime(app.getCreateTime());
        vo.setReviewTime(app.getReviewTime());

        for (AppointmentStatus as : AppointmentStatus.values()) {
            if (as.getCode() == app.getStatus()) {
                vo.setStatusDesc(as.getDesc());
                break;
            }
        }

        TimeConfig tc = timeConfigService.getById(app.getTimeSlotId());
        if (tc != null) {
            vo.setTimeSlotName(tc.getSlotName());
        }

        if (app.getFormId() != null) {
            FirstVisitForm form = formService.getById(app.getFormId());
            if (form != null) {
                vo.setTotalScore(form.getTotalScore());
                vo.setIsUrgent(form.getIsUrgent());
                vo.setStudentName(form.getStudentName());
                vo.setStudentNo(form.getStudentNo());
            }
        }

        vo.setVisitorName("初访员" + app.getVisitorId());
        return vo;
    }
}
