package org.example.consultation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.consultation.entity.FirstVisitResult;

import java.util.List;

public interface FirstVisitResultService extends IService<FirstVisitResult> {

    /** 初访员录入评估结果 */
    FirstVisitResult record(Long visitorId, FirstVisitResult result);

    /** 心理助理查看待安排咨询的初访结果（结论=安排咨询） */
    List<FirstVisitResult> listPendingArrangement();
}
