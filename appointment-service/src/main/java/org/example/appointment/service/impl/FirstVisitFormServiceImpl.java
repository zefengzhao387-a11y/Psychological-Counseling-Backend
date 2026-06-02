package org.example.appointment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.appointment.entity.FirstVisitForm;
import org.example.appointment.mapper.FirstVisitFormMapper;
import org.example.appointment.service.FirstVisitFormService;
import org.example.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FirstVisitFormServiceImpl extends ServiceImpl<FirstVisitFormMapper, FirstVisitForm> implements FirstVisitFormService {

    /** 计分报警阈值 */
    private static final int URGENT_SCORE_THRESHOLD = 60;

    @Override
    @Transactional
    public FirstVisitForm submit(FirstVisitForm form) {
        // 计分逻辑：根据 JSON 问卷中的各题分数求和
        int totalScore = calculateScore(form.getQuestionnaire());
        form.setTotalScore(totalScore);
        // 总分超过阈值标记为紧急
        form.setIsUrgent(totalScore >= URGENT_SCORE_THRESHOLD ? 1 : 0);
        form.setHasReadConsent(0);
        save(form);
        return form;
    }

    @Override
    @Transactional
    public void confirmConsent(Long formId, Long studentId) {
        FirstVisitForm form = getById(formId);
        if (form == null || !form.getStudentId().equals(studentId)) {
            throw new BusinessException("登记表不存在");
        }
        form.setHasReadConsent(1);
        form.setConsentTime(LocalDateTime.now());
        updateById(form);
    }

    /** 从 JSON 问卷中计算总分（问卷格式：{"scores":[3,4,5,2,...]}） */
    private int calculateScore(String questionnaire) {
        if (questionnaire == null || questionnaire.isEmpty()) {
            return 0;
        }
        try {
            // 使用简单的 JSON 解析（也可用 Hutool 的 JSONUtil）
            cn.hutool.json.JSONObject json = cn.hutool.json.JSONUtil.parseObj(questionnaire);
            cn.hutool.json.JSONArray scores = json.getJSONArray("scores");
            if (scores == null) return 0;
            int total = 0;
            for (Object s : scores) {
                total += Integer.parseInt(s.toString());
            }
            return total;
        } catch (Exception e) {
            return 0;
        }
    }
}
