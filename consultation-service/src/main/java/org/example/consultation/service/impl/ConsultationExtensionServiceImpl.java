package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.BusinessException;
import org.example.consultation.dto.ApproveDTO;
import org.example.consultation.dto.ExtensionDTO;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.entity.ConsultationExtension;
import org.example.consultation.mapper.ConsultationExtensionMapper;
import org.example.consultation.service.ConsultationAppointmentService;
import org.example.consultation.service.ConsultationExtensionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultationExtensionServiceImpl extends ServiceImpl<ConsultationExtensionMapper, ConsultationExtension>
        implements ConsultationExtensionService {

    private final ConsultationAppointmentService appointmentService;

    public ConsultationExtensionServiceImpl(ConsultationAppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    @Transactional
    public ConsultationExtension apply(Long counselorId, ExtensionDTO dto) {
        ConsultationAppointment app = appointmentService.getById(dto.getAppointmentId());
        if (app == null) throw new BusinessException("咨询安排不存在");
        if (app.getStatus() != 1) throw new BusinessException("仅进行中的咨询可申请追加");

        ConsultationExtension ext = new ConsultationExtension();
        ext.setAppointmentId(dto.getAppointmentId());
        ext.setCounselorId(counselorId);
        ext.setExtendWeeks(dto.getExtendWeeks());
        ext.setReason(dto.getReason());
        ext.setStatus(1); // 待审批
        save(ext);
        return ext;
    }

    @Override
    @Transactional
    public void approve(Long approverId, ApproveDTO dto) {
        ConsultationExtension ext = getById(dto.getExtensionId());
        if (ext == null || ext.getStatus() != 1) {
            throw new BusinessException("申请不存在或已审批");
        }

        ext.setApproverId(approverId);
        ext.setApproveTime(LocalDateTime.now());

        if (dto.getStatus() == 2) { // 通过
            ext.setStatus(2);
            // 追加周数
            ConsultationAppointment app = appointmentService.getById(ext.getAppointmentId());
            app.setRemainingWeeks(app.getRemainingWeeks() + ext.getExtendWeeks());
            app.setOccupiedWeeks(app.getOccupiedWeeks() + ext.getExtendWeeks());
            appointmentService.updateById(app);
        } else {
            ext.setStatus(3); // 拒绝
            ext.setApproveRemark(dto.getRemark());
        }
        updateById(ext);
    }

    @Override
    public List<ConsultationExtension> listPending() {
        return lambdaQuery().eq(ConsultationExtension::getStatus, 1)
                .orderByAsc(ConsultationExtension::getCreateTime).list();
    }
}
