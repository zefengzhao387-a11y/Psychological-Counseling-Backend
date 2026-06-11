package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.BusinessException;
import org.example.common.feign.dto.StudentProfileBriefDTO;
import org.example.common.support.StudentDisplaySupport;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.entity.FirstVisitResult;
import org.example.consultation.mapper.ConsultationAppointmentMapper;
import org.example.consultation.mapper.FirstVisitResultMapper;
import org.example.consultation.service.FirstVisitResultService;
import org.example.common.feign.AppointmentFeignClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FirstVisitResultServiceImpl extends ServiceImpl<FirstVisitResultMapper, FirstVisitResult>
        implements FirstVisitResultService {

    private final ConsultationAppointmentMapper consultationAppointmentMapper;
    private final AppointmentFeignClient appointmentFeignClient;
    private final StudentDisplaySupport studentDisplaySupport;

    public FirstVisitResultServiceImpl(ConsultationAppointmentMapper consultationAppointmentMapper,
                                       AppointmentFeignClient appointmentFeignClient,
                                       StudentDisplaySupport studentDisplaySupport) {
        this.consultationAppointmentMapper = consultationAppointmentMapper;
        this.appointmentFeignClient = appointmentFeignClient;
        this.studentDisplaySupport = studentDisplaySupport;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FirstVisitResult record(Long visitorId, FirstVisitResult result) {
        if (visitorId == null) {
            throw new BusinessException("未登录或登录已失效，请重新登录");
        }
        if (result.getAppointmentId() == null || result.getStudentId() == null) {
            throw new BusinessException("预约信息不完整");
        }

        long exists = lambdaQuery()
                .eq(FirstVisitResult::getAppointmentId, result.getAppointmentId())
                .count();
        if (exists > 0) {
            throw new BusinessException("该预约已评估，请勿重复录入");
        }

        var apptResp = appointmentFeignClient.getAppointmentBrief(result.getAppointmentId());
        if (apptResp == null || apptResp.getCode() != 200 || apptResp.getData() == null) {
            throw new BusinessException("关联预约不存在");
        }
        if (!visitorId.equals(apptResp.getData().getVisitorId())) {
            throw new BusinessException("只能评估分配给自己的预约");
        }

        result.setVisitorId(visitorId);
        if (result.getVisitTime() == null) {
            result.setVisitTime(LocalDateTime.now());
        }
        if (result.getAssistantStatus() == null) {
            result.setAssistantStatus(0);
        }
        save(result);

        completeFirstVisitAppointment(result.getAppointmentId());

        return result;
    }

    private void completeFirstVisitAppointment(Long appointmentId) {
        Exception lastError = null;
        for (int i = 0; i < 3; i++) {
            try {
                var completeResp = appointmentFeignClient.completeFirstVisit(appointmentId);
                if (completeResp != null && completeResp.getCode() == 200) {
                    return;
                }
                lastError = new BusinessException("预约状态更新失败");
            } catch (Exception e) {
                lastError = e;
            }
        }
        throw new BusinessException("评估已保存，但预约状态更新失败，请联系管理员"
                + (lastError != null && lastError.getMessage() != null ? "：" + lastError.getMessage() : ""));
    }

    @Override
    public List<FirstVisitResult> listPendingArrangement() {
        return listAssistantTasks().stream()
                .filter(r -> r.getConclusion() != null && r.getConclusion() == 2)
                .collect(Collectors.toList());
    }

    @Override
    public List<FirstVisitResult> listAssistantTasks() {
        return lambdaQuery()
                .and(w -> w.isNull(FirstVisitResult::getAssistantStatus)
                        .or().eq(FirstVisitResult::getAssistantStatus, 0))
                .orderByAsc(FirstVisitResult::getCreateTime)
                .list().stream()
                .filter(this::needsAssistantAction)
                .map(this::enrichStudentInfo)
                .collect(Collectors.toList());
    }

    @Override
    public void markProcessed(Long id) {
        FirstVisitResult result = getById(id);
        if (result == null) {
            throw new BusinessException("初访结果不存在");
        }
        if (result.getConclusion() != null && result.getConclusion() == 2) {
            throw new BusinessException("安排咨询类记录请通过「安排咨询」处理");
        }
        result.setAssistantStatus(2);
        updateById(result);
    }

    @Override
    public List<FirstVisitResult> listMyByVisitor(Long visitorId) {
        if (visitorId == null) {
            throw new BusinessException("未登录或登录已失效，请重新登录");
        }
        return lambdaQuery()
                .eq(FirstVisitResult::getVisitorId, visitorId)
                .orderByDesc(FirstVisitResult::getCreateTime)
                .list().stream()
                .map(this::enrichStudentInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<FirstVisitResult> listByStudent(Long studentId) {
        if (studentId == null) {
            return List.of();
        }
        return lambdaQuery()
                .eq(FirstVisitResult::getStudentId, studentId)
                .orderByDesc(FirstVisitResult::getCreateTime)
                .list().stream()
                .map(this::enrichStudentInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> listEvaluatedAppointmentIds(Long visitorId) {
        if (visitorId == null) {
            return List.of();
        }
        return lambdaQuery()
                .eq(FirstVisitResult::getVisitorId, visitorId)
                .isNotNull(FirstVisitResult::getAppointmentId)
                .list()
                .stream()
                .map(FirstVisitResult::getAppointmentId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> listAllEvaluatedAppointmentIds() {
        return lambdaQuery()
                .isNotNull(FirstVisitResult::getAppointmentId)
                .list()
                .stream()
                .map(FirstVisitResult::getAppointmentId)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean needsAssistantAction(FirstVisitResult result) {
        if (result.getConclusion() == null) {
            return false;
        }
        if (result.getConclusion() == 2) {
            Long count = consultationAppointmentMapper.selectCount(
                    new LambdaQueryWrapper<ConsultationAppointment>()
                            .eq(ConsultationAppointment::getFirstVisitResultId, result.getId()));
            return count == null || count == 0;
        }
        return result.getConclusion() == 1 || result.getConclusion() == 3;
    }

    private FirstVisitResult enrichStudentInfo(FirstVisitResult result) {
        if (result.getStudentId() == null) {
            return result;
        }
        StudentProfileBriefDTO profile = studentDisplaySupport.resolve(result.getStudentId());
        result.setStudentName(profile.getStudentName());
        result.setStudentNo(profile.getStudentNo());
        return result;
    }
}
