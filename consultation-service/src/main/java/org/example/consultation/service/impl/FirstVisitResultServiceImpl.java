package org.example.consultation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.common.exception.BusinessException;
import org.example.consultation.entity.FirstVisitResult;
import org.example.consultation.mapper.FirstVisitResultMapper;
import org.example.consultation.service.FirstVisitResultService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FirstVisitResultServiceImpl extends ServiceImpl<FirstVisitResultMapper, FirstVisitResult>
        implements FirstVisitResultService {

    @Override
    public FirstVisitResult record(Long visitorId, FirstVisitResult result) {
        result.setVisitorId(visitorId);
        result.setVisitTime(LocalDateTime.now());
        save(result);
        return result;
    }

    @Override
    public List<FirstVisitResult> listPendingArrangement() {
        // 初访结论=2（安排咨询）且尚未被安排
        return lambdaQuery()
                .eq(FirstVisitResult::getConclusion, 2)
                .orderByAsc(FirstVisitResult::getCreateTime)
                .list();
    }
}
