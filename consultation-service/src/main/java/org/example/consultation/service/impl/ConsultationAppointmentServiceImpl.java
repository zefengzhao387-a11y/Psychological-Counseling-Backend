package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.BusinessException;
import org.example.common.feign.AppointmentFeignClient;
import org.example.common.feign.UserFeignClient;
import org.example.common.feign.dto.CounselorBriefDTO;
import org.example.common.feign.dto.StudentProfileBriefDTO;
import org.example.common.feign.dto.TimeSlotBriefDTO;
import org.example.common.result.R;
import org.example.consultation.dto.ArrangeDTO;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.entity.ClosingReport;
import org.example.consultation.entity.FirstVisitResult;
import org.example.consultation.mapper.ConsultationAppointmentMapper;
import org.example.consultation.mapper.ClosingReportMapper;
import org.example.consultation.service.ConsultationAppointmentService;
import org.example.consultation.service.FirstVisitResultService;
import org.example.common.support.NotificationSupport;
import org.example.common.support.StudentDisplaySupport;
import org.example.common.support.UserLookupSupport;
import org.example.consultation.util.WeeklySlotUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConsultationAppointmentServiceImpl extends ServiceImpl<ConsultationAppointmentMapper, ConsultationAppointment>
        implements ConsultationAppointmentService {

    private static final int DEFAULT_WEEKS = 8;

    private final NotificationSupport notificationSupport;
    private final FirstVisitResultService firstVisitResultService;
    private final UserLookupSupport userLookupSupport;
    private final AppointmentFeignClient appointmentFeignClient;
    private final UserFeignClient userFeignClient;
    private final StudentDisplaySupport studentDisplaySupport;
    private final ClosingReportMapper closingReportMapper;

    public ConsultationAppointmentServiceImpl(NotificationSupport notificationSupport,
                                              FirstVisitResultService firstVisitResultService,
                                              UserLookupSupport userLookupSupport,
                                              AppointmentFeignClient appointmentFeignClient,
                                              UserFeignClient userFeignClient,
                                              StudentDisplaySupport studentDisplaySupport,
                                              ClosingReportMapper closingReportMapper) {
        this.notificationSupport = notificationSupport;
        this.firstVisitResultService = firstVisitResultService;
        this.userLookupSupport = userLookupSupport;
        this.appointmentFeignClient = appointmentFeignClient;
        this.userFeignClient = userFeignClient;
        this.studentDisplaySupport = studentDisplaySupport;
        this.closingReportMapper = closingReportMapper;
    }

    @Override
    @Transactional
    public ConsultationAppointment arrange(ArrangeDTO dto) {
        int weeks = dto.getOccupiedWeeks() != null ? dto.getOccupiedWeeks() : DEFAULT_WEEKS;
        Long counselorId = dto.getCounselorId();
        if (counselorId == null) {
            counselorId = matchAvailableCounselor(dto.getTimeSlotId(), dto.getStartDate(), weeks);
        }
        assertNoWeeklyConflict(counselorId, dto.getTimeSlotId(), dto.getStartDate(), weeks, null);

        ConsultationAppointment app = new ConsultationAppointment();
        app.setStudentId(dto.getStudentId());
        app.setFirstVisitResultId(dto.getResultId());
        app.setCounselorId(counselorId);
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

        String phone = userLookupSupport.getPhone(dto.getStudentId());
        notificationSupport.sendSms(
                phone,
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
    public Page<ConsultationAppointment> listAll(Integer page, Integer size, Long counselorId) {
        var query = lambdaQuery();
        if (counselorId != null) {
            query.eq(ConsultationAppointment::getCounselorId, counselorId);
        }
        Page<ConsultationAppointment> pageResult = query.orderByDesc(ConsultationAppointment::getCreateTime)
                .page(new Page<>(page, size));
        Map<Long, String> timeSlotNames = loadTimeSlotNames();
        pageResult.setRecords(pageResult.getRecords().stream()
                .map(item -> enrichDisplayInfo(item, timeSlotNames))
                .collect(Collectors.toList()));
        return pageResult;
    }

    @Override
    public Page<ConsultationAppointment> listClosableForReport(Integer page, Integer size, Long counselorId) {
        List<Long> reportedAppointmentIds = closingReportMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClosingReport>()
                                .isNotNull(ClosingReport::getAppointmentId)
                                .select(ClosingReport::getAppointmentId))
                .stream()
                .map(ClosingReport::getAppointmentId)
                .distinct()
                .collect(Collectors.toList());

        var query = lambdaQuery()
                .in(ConsultationAppointment::getStatus, 2, 3);
        if (counselorId != null) {
            query.eq(ConsultationAppointment::getCounselorId, counselorId);
        }
        if (!reportedAppointmentIds.isEmpty()) {
            query.notIn(ConsultationAppointment::getId, reportedAppointmentIds);
        }

        Page<ConsultationAppointment> pageResult = query.orderByDesc(ConsultationAppointment::getCreateTime)
                .page(new Page<>(page, size));
        Map<Long, String> timeSlotNames = loadTimeSlotNames();
        pageResult.setRecords(pageResult.getRecords().stream()
                .map(item -> enrichDisplayInfo(item, timeSlotNames))
                .collect(Collectors.toList()));
        return pageResult;
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

        String phone = userLookupSupport.getPhone(app.getStudentId());
        notificationSupport.sendSms(phone,
                String.format("您的咨询安排已更新：%s 起，地点 %s。",
                        app.getStartDate(), app.getLocation()),
                "CONSULTATION_UPDATE");
    }

    @Override
    public Long matchAvailableCounselor(Long timeSlotId, LocalDate startDate, int weeks) {
        List<CounselorBriefDTO> counselors = listCounselorsByType(2);
        if (counselors.isEmpty()) {
            throw new BusinessException("暂无可用咨询师，请先在老师信息中维护");
        }
        for (CounselorBriefDTO c : counselors) {
            if (c.getUserId() == null) continue;
            try {
                assertNoWeeklyConflict(c.getUserId(), timeSlotId, startDate, weeks, null);
                return c.getUserId();
            } catch (BusinessException ignored) {
                // 尝试下一位
            }
        }
        throw new BusinessException("该时段所有咨询师均已占用，请更换日期或时间段");
    }

    @Override
    public void assertNoWeeklyConflict(Long counselorId, Long timeSlotId, LocalDate startDate,
                                       int weeks, Long excludeId) {
        assertNoWeeklyConflictInternal(counselorId, timeSlotId, startDate, weeks, excludeId);
    }

    private List<CounselorBriefDTO> listCounselorsByType(int type) {
        try {
            var resp = userFeignClient.listCounselors();
            if (resp != null && resp.getCode() == 200 && resp.getData() != null) {
                return resp.getData().stream()
                        .filter(c -> c.getType() != null && c.getType() == type)
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {
            // fall through
        }
        return List.of();
    }

    private void assertNoWeeklyConflictInternal(Long counselorId, Long timeSlotId, LocalDate startDate,
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

    private ConsultationAppointment enrichDisplayInfo(ConsultationAppointment app, Map<Long, String> timeSlotNames) {
        if (app.getStudentId() != null) {
            StudentProfileBriefDTO profile = studentDisplaySupport.resolve(app.getStudentId());
            app.setStudentName(profile.getStudentName());
            app.setStudentNo(profile.getStudentNo());
        }
        if (app.getCounselorId() != null) {
            app.setCounselorName(userLookupSupport.getDisplayName(app.getCounselorId()));
        }
        if (app.getTimeSlotId() != null) {
            app.setTimeSlotName(timeSlotNames.get(app.getTimeSlotId()));
        }
        return app;
    }

    private Map<Long, String> loadTimeSlotNames() {
        try {
            R<List<TimeSlotBriefDTO>> response = appointmentFeignClient.listTimeConfig();
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                Map<Long, String> map = new HashMap<>();
                for (TimeSlotBriefDTO slot : response.getData()) {
                    if (slot.getId() != null) {
                        map.put(slot.getId(), slot.getSlotName());
                    }
                }
                return map;
            }
        } catch (Exception ignored) {
            // 时段名称获取失败时仍返回其它展示字段
        }
        return Map.of();
    }
}
