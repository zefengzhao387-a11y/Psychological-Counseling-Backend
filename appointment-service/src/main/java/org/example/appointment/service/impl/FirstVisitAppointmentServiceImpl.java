package org.example.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.appointment.dto.*;
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
import org.example.common.feign.ConsultationFeignClient;
import org.example.common.feign.dto.AppointmentProgressDTO;
import org.example.common.feign.dto.FirstVisitAppointmentBriefDTO;
import org.example.common.support.NotificationSupport;
import org.example.common.support.UserLookupSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FirstVisitAppointmentServiceImpl extends ServiceImpl<FirstVisitAppointmentMapper, FirstVisitAppointment>
        implements FirstVisitAppointmentService {

    private final FirstVisitFormService formService;
    private final DutyScheduleService dutyScheduleService;
    private final TimeConfigService timeConfigService;
    private final UserLookupSupport userLookupSupport;
    private final NotificationSupport notificationSupport;
    private final ObjectProvider<ConsultationFeignClient> consultationFeignClient;

    public FirstVisitAppointmentServiceImpl(FirstVisitFormService formService,
                                             DutyScheduleService dutyScheduleService,
                                             TimeConfigService timeConfigService,
                                             UserLookupSupport userLookupSupport,
                                             NotificationSupport notificationSupport,
                                             ObjectProvider<ConsultationFeignClient> consultationFeignClient) {
        this.formService = formService;
        this.dutyScheduleService = dutyScheduleService;
        this.timeConfigService = timeConfigService;
        this.userLookupSupport = userLookupSupport;
        this.notificationSupport = notificationSupport;
        this.consultationFeignClient = consultationFeignClient;
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
        if (ds.getCounselorType() == null || ds.getCounselorType() != 1) {
            throw new BusinessException("请选择初访员值班时段");
        }

        long count = lambdaQuery()
                .eq(FirstVisitAppointment::getStudentId, studentId)
                .in(FirstVisitAppointment::getStatus, 1, 2)
                .count();
        if (count > 0) {
            throw new BusinessException("您已有进行中的预约，请勿重复预约");
        }

        FirstVisitForm form = formService.getById(formId);
        if (form == null) {
            throw new BusinessException("登记表不存在");
        }
        if (!form.getStudentId().equals(studentId)) {
            throw new BusinessException("无权使用此登记表");
        }
        if (form.getHasReadConsent() == null || form.getHasReadConsent() != 1) {
            throw new BusinessException("请先阅读并签署知情同意书后再预约");
        }
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
        repairCompletedStatus();
        LambdaQueryWrapper<FirstVisitAppointment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(FirstVisitAppointment::getStatus, status);
        }
        wrapper.orderByDesc(FirstVisitAppointment::getIsPriority)
               .orderByAsc(FirstVisitAppointment::getCreateTime);
        Page<FirstVisitAppointment> pageResult = page(new Page<>(page, size), wrapper);
        Page<AppointmentVO> voPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        List<AppointmentVO> records = pageResult.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        enrichProgress(records);
        voPage.setRecords(records);
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

        if (dto.getStatus() == AppointmentStatus.APPROVED.getCode()) {
            String phone = userLookupSupport.getPhone(app.getStudentId());
            TimeConfig tc = timeConfigService.getById(app.getTimeSlotId());
            String slotName = tc != null ? tc.getSlotName() : "";
            String visitorName = userLookupSupport.getDisplayName(app.getVisitorId());
            notificationSupport.sendSms(phone,
                    String.format("您的初访预约已通过：%s %s，地点 %s，初访员 %s。",
                            app.getAppointmentDate(), slotName,
                            dto.getLocation() != null ? dto.getLocation() : app.getLocation(),
                            visitorName),
                    "FIRST_VISIT_APPROVED");
        } else if (dto.getStatus() == AppointmentStatus.REJECTED.getCode()) {
            String phone = userLookupSupport.getPhone(app.getStudentId());
            notificationSupport.sendSms(phone,
                    "您的初访预约未通过审核，如有疑问请联系心理中心。",
                    "FIRST_VISIT_REJECTED");
        }
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
        Long oldDutyId = app.getDutyScheduleId();
        Long newDutyId = oldDutyId;

        if (dto.getAppointmentDate() != null || dto.getTimeSlotId() != null || dto.getVisitorId() != null) {
            LocalDate targetDate = dto.getAppointmentDate() != null ? dto.getAppointmentDate() : app.getAppointmentDate();
            Long targetSlot = dto.getTimeSlotId() != null ? dto.getTimeSlotId() : app.getTimeSlotId();
            newDutyId = dutyScheduleService.matchAvailableVisitor(targetDate, targetSlot, dto.getVisitorId());
            DutySchedule newDuty = dutyScheduleService.getById(newDutyId);
            if (dto.getVisitorId() != null) {
                app.setVisitorId(dto.getVisitorId());
            } else if (newDuty != null) {
                app.setVisitorId(newDuty.getCounselorId());
            }
            app.setDutyScheduleId(newDutyId);
        }
        if (dto.getAppointmentDate() != null) app.setAppointmentDate(dto.getAppointmentDate());
        if (dto.getTimeSlotId() != null) app.setTimeSlotId(dto.getTimeSlotId());
        if (dto.getLocation() != null) app.setLocation(dto.getLocation());
        updateById(app);

        if (oldDutyId != null && !oldDutyId.equals(newDutyId)) {
            dutyScheduleService.decrementBooked(oldDutyId);
            dutyScheduleService.incrementBooked(newDutyId);
        }

        if (app.getStatus() == AppointmentStatus.APPROVED.getCode()) {
            notifyReschedule(app);
        }
    }

    private void notifyReschedule(FirstVisitAppointment app) {
        String phone = userLookupSupport.getPhone(app.getStudentId());
        TimeConfig tc = timeConfigService.getById(app.getTimeSlotId());
        String slotName = tc != null ? tc.getSlotName() : "";
        String visitorName = userLookupSupport.getDisplayName(app.getVisitorId());
        notificationSupport.sendSms(phone,
                String.format("您的初访预约已改约：%s %s，地点 %s，初访员 %s。",
                        app.getAppointmentDate(), slotName,
                        app.getLocation() != null ? app.getLocation() : "待定",
                        visitorName),
                "FIRST_VISIT_RESCHEDULE");
    }

    private void notifyApprovedAppointment(FirstVisitAppointment app) {
        String phone = userLookupSupport.getPhone(app.getStudentId());
        TimeConfig tc = timeConfigService.getById(app.getTimeSlotId());
        String slotName = tc != null ? tc.getSlotName() : "";
        String visitorName = userLookupSupport.getDisplayName(app.getVisitorId());
        notificationSupport.sendSms(phone,
                String.format("您的初访预约已确认：%s %s，地点 %s，初访员 %s。",
                        app.getAppointmentDate(), slotName,
                        app.getLocation() != null ? app.getLocation() : "待定",
                        visitorName),
                "FIRST_VISIT_CONFIRMED");
    }

    @Override
    @Transactional
    public void addAppointment(AppointmentAddDTO dto, Long adminId) {
        Long visitorId = dto.getVisitorId();
        Long dutyScheduleId = null;
        if (visitorId == null) {
            dutyScheduleId = dutyScheduleService.matchVisitor(dto);
            DutySchedule ds = dutyScheduleService.getById(dutyScheduleId);
            visitorId = ds.getCounselorId();
        }

        FirstVisitAppointment app = new FirstVisitAppointment();
        app.setStudentId(dto.getStudentId());
        app.setFormId(dto.getFormId());
        app.setVisitorId(visitorId);
        app.setDutyScheduleId(dutyScheduleId);
        app.setAppointmentDate(dto.getAppointmentDate());
        app.setTimeSlotId(dto.getTimeSlotId());
        app.setLocation(dto.getLocation());
        app.setStatus(AppointmentStatus.APPROVED.getCode());
        app.setReviewerId(adminId);
        app.setReviewTime(LocalDateTime.now());
        save(app);

        if (dutyScheduleId != null) {
            dutyScheduleService.incrementBooked(dutyScheduleId);
        }
        notifyApprovedAppointment(app);
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

    @Override
    @Transactional
    public void backupAppointment(BackupAppointmentDTO dto, Long adminId) {
        // 查找或创建备班值班
        Long dutyScheduleId = dutyScheduleService.findOrCreateBackupSlot(
                dto.getVisitorId(), dto.getAppointmentDate(), dto.getTimeSlotId());

        FirstVisitAppointment app = new FirstVisitAppointment();
        app.setStudentId(dto.getStudentId());
        app.setVisitorId(dto.getVisitorId());
        app.setDutyScheduleId(dutyScheduleId);
        app.setAppointmentDate(dto.getAppointmentDate());
        app.setTimeSlotId(dto.getTimeSlotId());
        app.setLocation(dto.getLocation());
        app.setStatus(AppointmentStatus.APPROVED.getCode());
        app.setReviewerId(adminId);
        app.setReviewTime(LocalDateTime.now());
        save(app);

        dutyScheduleService.incrementBooked(dutyScheduleId);
        notifyApprovedAppointment(app);
    }

    @Override
    public List<StudentSearchVO> searchStudent(String keyword) {
        return formService.searchByKeyword(keyword);
    }

    @Override
    public List<AppointmentVO> listForVisitor(Long visitorId) {
        if (visitorId == null) {
            throw new BusinessException("未登录或登录已失效，请重新登录");
        }
        // 仅展示「已通过、待初访」的预约；评估完成后 status 变为 5（已完成）
        return lambdaQuery()
                .eq(FirstVisitAppointment::getVisitorId, visitorId)
                .eq(FirstVisitAppointment::getStatus, AppointmentStatus.APPROVED.getCode())
                .orderByAsc(FirstVisitAppointment::getAppointmentDate)
                .orderByAsc(FirstVisitAppointment::getTimeSlotId)
                .list()
                .stream()
                .map(this::toVO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void markCompleted(Long appointmentId) {
        FirstVisitAppointment app = getById(appointmentId);
        if (app == null) {
            throw new BusinessException("预约不存在");
        }
        if (app.getStatus() != AppointmentStatus.APPROVED.getCode()) {
            return;
        }
        app.setStatus(AppointmentStatus.COMPLETED.getCode());
        updateById(app);
    }

    @Override
    public FirstVisitAppointmentBriefDTO getBrief(Long appointmentId) {
        FirstVisitAppointment app = getById(appointmentId);
        if (app == null) {
            throw new BusinessException("预约不存在");
        }
        FirstVisitAppointmentBriefDTO dto = new FirstVisitAppointmentBriefDTO();
        dto.setId(app.getId());
        dto.setStudentId(app.getStudentId());
        dto.setVisitorId(app.getVisitorId());
        dto.setStatus(app.getStatus());
        return dto;
    }

    /** 将已有评估记录但状态仍为「已通过」的预约补标为已完成 */
    private void repairCompletedStatus() {
        ConsultationFeignClient client = consultationFeignClient.getIfAvailable();
        if (client == null) {
            return;
        }
        try {
            var resp = client.listAllEvaluatedAppointmentIds();
            if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
                return;
            }
            for (Long appointmentId : resp.getData()) {
                markCompleted(appointmentId);
            }
        } catch (Exception e) {
            log.warn("补同步初访完成状态失败: {}", e.getMessage());
        }
    }

    private AppointmentVO toVO(FirstVisitAppointment app) {
        AppointmentVO vo = new AppointmentVO();
        vo.setId(app.getId());
        vo.setStudentId(app.getStudentId());
        vo.setFormId(app.getFormId());
        vo.setVisitorId(app.getVisitorId());
        vo.setAppointmentDate(app.getAppointmentDate());
        vo.setTimeSlotId(app.getTimeSlotId());
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
        if (vo.getStudentName() == null || vo.getStudentName().contains("?")) {
            vo.setStudentName(userLookupSupport.getDisplayName(app.getStudentId()));
        }

        vo.setVisitorName(userLookupSupport.getDisplayName(app.getVisitorId()));
        return vo;
    }

    private void enrichProgress(List<AppointmentVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (AppointmentVO vo : records) {
            vo.setFirstVisitProgress(resolveFirstVisitProgress(vo.getStatus()));
        }
        ConsultationFeignClient client = consultationFeignClient.getIfAvailable();
        if (client == null) {
            for (AppointmentVO vo : records) {
                vo.setConsultationProgress("—");
                vo.setClosingProgress("—");
            }
            return;
        }
        try {
            String ids = records.stream().map(r -> String.valueOf(r.getId())).collect(Collectors.joining(","));
            var resp = client.listProgressByAppointments(ids);
            Map<Long, AppointmentProgressDTO> progressMap = resp != null && resp.getCode() == 200 && resp.getData() != null
                    ? resp.getData().stream().collect(Collectors.toMap(AppointmentProgressDTO::getAppointmentId, p -> p, (a, b) -> a))
                    : Map.of();
            for (AppointmentVO vo : records) {
                Integer st = vo.getStatus();
                if (st != null && (st == AppointmentStatus.PENDING.getCode()
                        || st == AppointmentStatus.REJECTED.getCode()
                        || st == AppointmentStatus.CANCELLED.getCode())) {
                    vo.setConsultationProgress("—");
                    vo.setClosingProgress("—");
                    continue;
                }
                AppointmentProgressDTO p = progressMap.get(vo.getId());
                if (p != null) {
                    vo.setConsultationProgress(p.getConsultationProgress());
                    vo.setClosingProgress(p.getClosingProgress());
                } else {
                    vo.setConsultationProgress(st != null && st == AppointmentStatus.APPROVED.getCode() ? "待评估" : "—");
                    vo.setClosingProgress("—");
                }
            }
        } catch (Exception e) {
            log.warn("拉取咨询进度失败: {}", e.getMessage());
            for (AppointmentVO vo : records) {
                if (vo.getConsultationProgress() == null) {
                    vo.setConsultationProgress("—");
                }
                if (vo.getClosingProgress() == null) {
                    vo.setClosingProgress("—");
                }
            }
        }
    }

    private String resolveFirstVisitProgress(Integer status) {
        if (status == null) {
            return "—";
        }
        for (AppointmentStatus as : AppointmentStatus.values()) {
            if (as.getCode() == status) {
                if (as == AppointmentStatus.APPROVED) {
                    return "待初访";
                }
                return as.getDesc();
            }
        }
        return "—";
    }
}
