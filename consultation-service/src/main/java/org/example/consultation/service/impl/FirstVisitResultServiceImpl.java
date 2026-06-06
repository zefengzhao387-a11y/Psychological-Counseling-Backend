package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.BusinessException;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.entity.FirstVisitResult;
import org.example.consultation.mapper.ConsultationAppointmentMapper;
import org.example.consultation.mapper.FirstVisitResultMapper;
import org.example.consultation.service.FirstVisitResultService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FirstVisitResultServiceImpl extends ServiceImpl<FirstVisitResultMapper, FirstVisitResult>
        implements FirstVisitResultService {

    private final ConsultationAppointmentMapper consultationAppointmentMapper;

    public FirstVisitResultServiceImpl(ConsultationAppointmentMapper consultationAppointmentMapper) {
        this.consultationAppointmentMapper = consultationAppointmentMapper;
    }

    @Override
    public FirstVisitResult record(Long visitorId, FirstVisitResult result) {
        result.setVisitorId(visitorId);
        result.setVisitTime(LocalDateTime.now());
        if (result.getAssistantStatus() == null) {
            result.setAssistantStatus(0);
        }
        save(result);
        return result;
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
}
