package org.example.consultation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.consultation.dto.RecordDTO;
import org.example.consultation.entity.ConsultationRecord;

import java.util.List;

public interface ConsultationRecordService extends IService<ConsultationRecord> {

    /**
     * 咨询师录入咨询记录：
     * - 支持第 1~8 次及 9+ 次
     * - 状态为"结案"时自动触发结案流程
     * - 状态为"脱落"时释放剩余时段
     */
    ConsultationRecord record(Long counselorId, RecordDTO dto);

    /** 查看某安排的咨询记录列表 */
    List<ConsultationRecord> listByAppointment(Long appointmentId);
}
