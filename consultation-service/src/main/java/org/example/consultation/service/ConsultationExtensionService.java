package org.example.consultation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.consultation.dto.ApproveDTO;
import org.example.consultation.dto.ExtensionDTO;
import org.example.consultation.entity.ConsultationExtension;

import java.util.List;

public interface ConsultationExtensionService extends IService<ConsultationExtension> {

    /** 咨询师提交追加时段申请 */
    ConsultationExtension apply(Long counselorId, ExtensionDTO dto);

    /** 管理员审批 */
    void approve(Long approverId, ApproveDTO dto);

    /** 待审批列表 */
    List<ConsultationExtension> listPending();
}
