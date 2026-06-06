package org.example.consultation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.consultation.entity.FirstVisitResult;

import java.util.List;

public interface FirstVisitResultService extends IService<FirstVisitResult> {

    FirstVisitResult record(Long visitorId, FirstVisitResult result);

    /** 待安排咨询（结论=安排咨询 且 助理未处理） */
    List<FirstVisitResult> listPendingArrangement();

    /** 心理助理待办（含无需咨询/转介待标记 + 待安排咨询） */
    List<FirstVisitResult> listAssistantTasks();

    /** 标记已处理（无需安排/转介归档） */
    void markProcessed(Long id);
}
