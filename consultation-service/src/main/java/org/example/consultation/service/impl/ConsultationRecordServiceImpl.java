package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.BusinessException;
import org.example.consultation.dto.RecordDTO;
import org.example.consultation.entity.ConsultationAppointment;
import org.example.consultation.entity.ConsultationRecord;
import org.example.consultation.mapper.ConsultationRecordMapper;
import org.example.consultation.service.ConsultationAppointmentService;
import org.example.consultation.service.ConsultationRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultationRecordServiceImpl extends ServiceImpl<ConsultationRecordMapper, ConsultationRecord>
        implements ConsultationRecordService {

    private final ConsultationAppointmentService appointmentService;

    public ConsultationRecordServiceImpl(ConsultationAppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    @Transactional
    public ConsultationRecord record(Long counselorId, RecordDTO dto) {
        ConsultationAppointment app = appointmentService.getById(dto.getAppointmentId());
        if (app == null) throw new BusinessException("咨询安排不存在");
        if (app.getRemainingWeeks() <= 0 && app.getStatus() != 1) {
            throw new BusinessException("该咨询安排已结束");
        }

        ConsultationRecord record = new ConsultationRecord();
        record.setAppointmentId(dto.getAppointmentId());
        record.setSessionNumber(dto.getSessionNumber());
        record.setConsultDate(dto.getConsultDate());
        record.setStatus(dto.getStatus());
        record.setContent(dto.getContent());
        record.setCounselorNote(dto.getCounselorNote());
        save(record);

        // 扣减剩余周数
        if (app.getRemainingWeeks() > 0) {
            app.setRemainingWeeks(app.getRemainingWeeks() - 1);
        }

        // 结案：释放剩余时段
        if (dto.getStatus() == 5) { // 结案
            app.setStatus(2);
            app.setRemainingWeeks(0);
        }
        // 脱落：释放
        if (dto.getStatus() == 4) { // 脱落
            app.setStatus(3);
            app.setRemainingWeeks(0);
        }

        appointmentService.updateById(app);
        return record;
    }

    @Override
    public List<ConsultationRecord> listByAppointment(Long appointmentId) {
        return lambdaQuery()
                .eq(ConsultationRecord::getAppointmentId, appointmentId)
                .orderByAsc(ConsultationRecord::getSessionNumber)
                .list();
    }
}
