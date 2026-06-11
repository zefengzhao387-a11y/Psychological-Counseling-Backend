package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.common.feign.dto.AppointmentProgressDTO;
import org.example.consultation.entity.ClosingReport;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.entity.FirstVisitResult;
import org.example.consultation.mapper.ClosingReportMapper;
import org.example.consultation.mapper.ConsultationAppointmentMapper;
import org.example.consultation.mapper.FirstVisitResultMapper;
import org.example.consultation.service.ConsultationProgressService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConsultationProgressServiceImpl implements ConsultationProgressService {

    private final FirstVisitResultMapper firstVisitResultMapper;
    private final ConsultationAppointmentMapper consultationAppointmentMapper;
    private final ClosingReportMapper closingReportMapper;

    public ConsultationProgressServiceImpl(FirstVisitResultMapper firstVisitResultMapper,
                                           ConsultationAppointmentMapper consultationAppointmentMapper,
                                           ClosingReportMapper closingReportMapper) {
        this.firstVisitResultMapper = firstVisitResultMapper;
        this.consultationAppointmentMapper = consultationAppointmentMapper;
        this.closingReportMapper = closingReportMapper;
    }

    @Override
    public List<AppointmentProgressDTO> listByAppointmentIds(List<Long> appointmentIds) {
        if (appointmentIds == null || appointmentIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = appointmentIds.stream().distinct().toList();

        List<FirstVisitResult> results = firstVisitResultMapper.selectList(
                new LambdaQueryWrapper<FirstVisitResult>().in(FirstVisitResult::getAppointmentId, ids));
        Map<Long, FirstVisitResult> resultByAppt = results.stream()
                .collect(Collectors.toMap(FirstVisitResult::getAppointmentId, Function.identity(), (a, b) -> a));

        List<Long> resultIds = results.stream().map(FirstVisitResult::getId).toList();
        Map<Long, ConsultationAppointment> consultByResultId = resultIds.isEmpty()
                ? Collections.emptyMap()
                : consultationAppointmentMapper.selectList(
                        new LambdaQueryWrapper<ConsultationAppointment>()
                                .in(ConsultationAppointment::getFirstVisitResultId, resultIds))
                .stream()
                .collect(Collectors.toMap(ConsultationAppointment::getFirstVisitResultId, Function.identity(), (a, b) -> a));

        List<Long> consultIds = consultByResultId.values().stream().map(ConsultationAppointment::getId).toList();
        Map<Long, ClosingReport> reportByConsultId = consultIds.isEmpty()
                ? Collections.emptyMap()
                : closingReportMapper.selectList(
                        new LambdaQueryWrapper<ClosingReport>().in(ClosingReport::getAppointmentId, consultIds))
                .stream()
                .collect(Collectors.toMap(ClosingReport::getAppointmentId, Function.identity(), (a, b) -> a));

        List<AppointmentProgressDTO> list = new ArrayList<>();
        for (Long apptId : ids) {
            AppointmentProgressDTO dto = new AppointmentProgressDTO();
            dto.setAppointmentId(apptId);
            FirstVisitResult result = resultByAppt.get(apptId);
            if (result == null) {
                dto.setConsultationProgress("待评估");
                dto.setClosingProgress("—");
            } else {
                dto.setConsultationProgress(resolveConsultation(result, consultByResultId.get(result.getId())));
                ConsultationAppointment consult = consultByResultId.get(result.getId());
                dto.setClosingProgress(resolveClosing(result, consult, consult != null ? reportByConsultId.get(consult.getId()) : null));
            }
            list.add(dto);
        }
        return list;
    }

    private String resolveConsultation(FirstVisitResult result, ConsultationAppointment consult) {
        Integer conclusion = result.getConclusion();
        if (conclusion == null) {
            return "待评估";
        }
        if (conclusion == 1) {
            return "无需咨询";
        }
        if (conclusion == 3) {
            return "转介送诊";
        }
        if (consult == null) {
            return result.getAssistantStatus() != null && result.getAssistantStatus() >= 1 ? "待安排" : "待安排";
        }
        return switch (consult.getStatus() != null ? consult.getStatus() : 0) {
            case 1 -> consult.getRemainingWeeks() != null && consult.getRemainingWeeks() > 0
                    ? "咨询中（剩" + consult.getRemainingWeeks() + "周）"
                    : "咨询中";
            case 2 -> "咨询已结案";
            case 3 -> "已脱落";
            default -> "咨询中";
        };
    }

    private String resolveClosing(FirstVisitResult result, ConsultationAppointment consult, ClosingReport report) {
        if (result.getConclusion() != null && (result.getConclusion() == 1 || result.getConclusion() == 3)) {
            return "—";
        }
        if (consult == null) {
            return "未开始";
        }
        if (consult.getStatus() != null && consult.getStatus() == 1) {
            return report == null ? "未开始" : mapReportStatus(report.getStatus());
        }
        if (report == null) {
            return consult.getStatus() != null && consult.getStatus() == 2 ? "待写报告" : "未开始";
        }
        return mapReportStatus(report.getStatus());
    }

    private String mapReportStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "未开始";
        }
        return switch (status) {
            case "草稿" -> "报告草稿";
            case "已提交" -> "报告已提交";
            case "已审核" -> "报告已审核";
            case "已驳回" -> "报告已驳回";
            default -> status;
        };
    }
}
