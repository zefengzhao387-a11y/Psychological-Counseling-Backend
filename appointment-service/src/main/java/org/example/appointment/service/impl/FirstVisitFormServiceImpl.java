package org.example.appointment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.appointment.dto.StudentSearchVO;
import org.example.appointment.entity.FirstVisitForm;
import org.example.appointment.mapper.FirstVisitFormMapper;
import org.example.appointment.service.FirstVisitFormService;
import org.example.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public List<StudentSearchVO> searchByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<FirstVisitForm> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(FirstVisitForm::getStudentNo, keyword)
               .or()
               .like(FirstVisitForm::getStudentName, keyword)
               .orderByDesc(FirstVisitForm::getCreateTime);

        List<FirstVisitForm> forms = list(wrapper);
        if (forms.isEmpty()) {
            return Collections.emptyList();
        }

        // 按 studentId 去重，保留每个学生最新的登记表
        Map<Long, FirstVisitForm> latestMap = new LinkedHashMap<>();
        for (FirstVisitForm form : forms) {
            latestMap.putIfAbsent(form.getStudentId(), form);
        }

        return latestMap.values().stream().map(form -> {
            StudentSearchVO vo = new StudentSearchVO();
            vo.setStudentId(form.getStudentId());
            vo.setStudentName(form.getStudentName());
            vo.setStudentNo(form.getStudentNo());
            vo.setDepartment(form.getDepartment());
            vo.setPhone(form.getPhone());
            vo.setFormId(form.getId());
            vo.setHasForm(true);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public FirstVisitForm getLatestByStudentId(Long studentId) {
        return lambdaQuery()
                .eq(FirstVisitForm::getStudentId, studentId)
                .orderByDesc(FirstVisitForm::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    /** 从 JSON 问卷中计算总分（问卷格式：{"scores":[3,4,5,2,...]}） */
    private int calculateScore(String questionnaire) {
        if (questionnaire == null || questionnaire.isEmpty()) {
            return 0;
        }
        try {
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
