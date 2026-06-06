package org.example.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.appointment.dto.AppointmentAddDTO;
import org.example.appointment.dto.AppointmentVO;
import org.example.appointment.dto.BackupAppointmentDTO;
import org.example.appointment.dto.RescheduleDTO;
import org.example.appointment.dto.ReviewDTO;
import org.example.appointment.dto.StudentSearchVO;
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

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
        // 1. 确定学生ID：优先使用 studentId，否则用 keyword 搜索
        Long studentId = dto.getStudentId();
        Long formId = dto.getFormId();

        if (studentId == null && (dto.getKeyword() != null && !dto.getKeyword().isBlank())) {
            // 按关键词搜索学生
            List<StudentSearchVO> students = formService.searchByKeyword(dto.getKeyword());
            if (students.isEmpty()) {
                throw new BusinessException("未找到匹配的学生，请检查学号或姓名");
            }
            if (students.size() > 1) {
                throw new BusinessException("找到多个匹配学生，请指定具体学生（匹配到 " + students.size() + " 人）");
            }
            StudentSearchVO vo = students.get(0);
            studentId = vo.getStudentId();
            if (formId == null) {
                formId = vo.getFormId();
            }
        }

        if (studentId == null) {
            throw new BusinessException("请指定学生（传 studentId 或 keyword）");
        }

        // 2. 确定登记表：若未传 formId，自动获取最新登记表
        if (formId == null) {
            FirstVisitForm latestForm = formService.getLatestByStudentId(studentId);
            if (latestForm == null) {
                throw new BusinessException("该学生尚未填写首访登记表，请先完成登记");
            }
            formId = latestForm.getId();
        }

        // 3. 验证登记表存在且属于该学生
        FirstVisitForm form = formService.getById(formId);
        if (form == null || !form.getStudentId().equals(studentId)) {
            throw new BusinessException("登记表不存在或不属于该学生");
        }

        // 4. 匹配空闲初访员：无排班时自动补备班（与补录备班逻辑一致）
        Long dutyScheduleId;
        Long visitorId = dto.getVisitorId();
        try {
            if (visitorId != null) {
                dutyScheduleId = dutyScheduleService.matchAvailableVisitor(
                        dto.getAppointmentDate(), dto.getTimeSlotId(), visitorId);
            } else {
                dutyScheduleId = dutyScheduleService.matchAvailableVisitor(
                        dto.getAppointmentDate(), dto.getTimeSlotId(), null);
            }
        } catch (BusinessException ex) {
            Long backupVisitor = visitorId != null ? visitorId : resolveDefaultVisitorId();
            dutyScheduleId = dutyScheduleService.findOrCreateBackupSlot(
                    backupVisitor, dto.getAppointmentDate(), dto.getTimeSlotId());
        }

        // 5. 获取匹配到的初访员ID
        DutySchedule ds = dutyScheduleService.getById(dutyScheduleId);
        visitorId = ds.getCounselorId();

        // 6. 创建预约记录（直接已通过）
        FirstVisitAppointment app = new FirstVisitAppointment();
        app.setStudentId(studentId);
        app.setFormId(formId);
        app.setVisitorId(visitorId);
        app.setDutyScheduleId(dutyScheduleId);
        app.setAppointmentDate(dto.getAppointmentDate());
        app.setTimeSlotId(dto.getTimeSlotId());
        app.setLocation(dto.getLocation());
        app.setStatus(AppointmentStatus.APPROVED.getCode());
        app.setReviewerId(adminId);
        app.setReviewTime(LocalDateTime.now());
        save(app);

        // 7. 更新值班已预约数
        dutyScheduleService.incrementBooked(dutyScheduleId);
    }

    /** 无指定初访员时，取系统中任意一名初访员（counselor_type=1） */
    private Long resolveDefaultVisitorId() {
        DutySchedule any = dutyScheduleService.lambdaQuery()
                .eq(DutySchedule::getCounselorType, 1)
                .last("LIMIT 1")
                .one();
        if (any != null) {
            return any.getCounselorId();
        }
        return 2L;
    }


    @Override
    public List<StudentSearchVO> searchStudent(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        return formService.searchByKeyword(keyword);
    }

    @Override
    @Transactional
    public void backupAppointment(BackupAppointmentDTO dto, Long adminId) {
        if (dto.getAppointmentDate() == null || dto.getTimeSlotId() == null) {
            throw new BusinessException("预约日期和时间段不能为空");
        }

        Long studentId = dto.getStudentId();
        Long formId = null;

        if (studentId == null && StringUtils.hasText(dto.getKeyword())) {
            List<StudentSearchVO> matches = formService.searchByKeyword(dto.getKeyword());
            if (matches.isEmpty()) {
                throw new BusinessException("未找到匹配的学生");
            }
            StudentSearchVO match = matches.get(0);
            studentId = match.getStudentId();
            formId = match.getFormId();
        }

        if (studentId == null) {
            throw new BusinessException("请指定学生或搜索关键词");
        }

        if (formId == null) {
            FirstVisitForm latest = formService.getLatestByStudentId(studentId);
            if (latest != null) {
                formId = latest.getId();
            } else if (StringUtils.hasText(dto.getStudentName()) && StringUtils.hasText(dto.getStudentNo())) {
                FirstVisitForm form = new FirstVisitForm();
                form.setStudentId(studentId);
                form.setStudentName(dto.getStudentName());
                form.setStudentNo(dto.getStudentNo());
                form.setHasReadConsent(1);
                form.setConsentTime(LocalDateTime.now());
                formService.save(form);
                formId = form.getId();
            }
        }

        Long dutyScheduleId = dutyScheduleService.matchAvailableVisitor(
                dto.getAppointmentDate(), dto.getTimeSlotId(), dto.getVisitorId());
        DutySchedule ds = dutyScheduleService.getById(dutyScheduleId);
        if (ds == null) {
            throw new BusinessException("未找到可用值班安排");
        }

        if (dto.getVisitorId() != null && !dto.getVisitorId().equals(ds.getCounselorId())) {
            dutyScheduleId = dutyScheduleService.findOrCreateBackupSlot(
                    dto.getVisitorId(), dto.getAppointmentDate(), dto.getTimeSlotId());
            ds = dutyScheduleService.getById(dutyScheduleId);
        } else if (dto.getVisitorId() == null) {
            dutyScheduleId = dutyScheduleService.findOrCreateBackupSlot(
                    ds.getCounselorId(), dto.getAppointmentDate(), dto.getTimeSlotId());
            ds = dutyScheduleService.getById(dutyScheduleId);
        }

        FirstVisitAppointment app = new FirstVisitAppointment();
        app.setStudentId(studentId);
        app.setFormId(formId);
        app.setDutyScheduleId(dutyScheduleId);
        app.setVisitorId(ds.getCounselorId());
        app.setAppointmentDate(dto.getAppointmentDate());
        app.setTimeSlotId(dto.getTimeSlotId());
        app.setLocation(dto.getLocation());
        app.setStatus(AppointmentStatus.APPROVED.getCode());
        app.setReviewerId(adminId);
        app.setReviewTime(LocalDateTime.now());
        app.setReviewRemark(dto.getRemark());
        save(app);

        dutyScheduleService.incrementBooked(dutyScheduleId);
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
