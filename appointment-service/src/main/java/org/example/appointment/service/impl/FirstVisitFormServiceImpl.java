package org.example.appointment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.appointment.dto.StudentSearchVO;
import org.example.appointment.entity.FirstVisitForm;
import org.example.appointment.mapper.FirstVisitFormMapper;
import org.example.appointment.service.FirstVisitFormService;
import org.example.common.context.UserContext;
import org.example.common.exception.BusinessException;
import org.example.common.feign.dto.UserBriefDTO;
import org.example.common.support.UserLookupSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FirstVisitFormServiceImpl extends ServiceImpl<FirstVisitFormMapper, FirstVisitForm> implements FirstVisitFormService {

    /** 计分报警阈值 */
    private static final int URGENT_SCORE_THRESHOLD = 60;

    private final UserLookupSupport userLookupSupport;

    public FirstVisitFormServiceImpl(UserLookupSupport userLookupSupport) {
        this.userLookupSupport = userLookupSupport;
    }

    @Override
    @Transactional
    public FirstVisitForm submit(FirstVisitForm form) {
        if (form.getStudentId() == null) {
            throw new BusinessException("未登录或登录已失效，请重新登录");
        }
        fillStudentProfile(form);
        // 计分逻辑：根据 JSON 问卷中的各题分数求和
        int totalScore = calculateScore(form.getQuestionnaire());
        form.setTotalScore(totalScore);
        // 总分超过阈值标记为紧急
        form.setIsUrgent(totalScore >= URGENT_SCORE_THRESHOLD ? 1 : 0);
        form.setHasReadConsent(0);
        save(form);
        return form;
    }

    /** 从用户服务同步学生基本信息，登记表不再重复填写 */
    private void fillStudentProfile(FirstVisitForm form) {
        Long studentId = form.getStudentId();
        UserBriefDTO user = userLookupSupport.getUser(studentId);

        if (user != null) {
            form.setStudentNo(StringUtils.hasText(user.getUserNo()) ? user.getUserNo() : defaultStudentNo(studentId));
            form.setStudentName(StringUtils.hasText(user.getUsername()) ? user.getUsername() : form.getStudentNo());
            form.setGender(user.getGender());
            form.setDepartment(user.getDepartment());
            form.setPhone(user.getPhone());
        } else {
            applyContextFallback(form, studentId);
        }

        if (!StringUtils.hasText(form.getStudentName())) {
            form.setStudentName(defaultStudentName(studentId));
        }
        if (!StringUtils.hasText(form.getStudentNo())) {
            form.setStudentNo(defaultStudentNo(studentId));
        }
        if (!StringUtils.hasText(form.getPhone())) {
            throw new BusinessException("请先在注册/个人信息中完善手机号后再提交登记表");
        }
    }

    private void applyContextFallback(FirstVisitForm form, Long studentId) {
        String username = UserContext.getUsername();
        form.setStudentName(StringUtils.hasText(username) ? username : defaultStudentName(studentId));
        form.setStudentNo(defaultStudentNo(studentId));
    }

    private String defaultStudentName(Long studentId) {
        return "学生" + studentId;
    }

    private String defaultStudentNo(Long studentId) {
        return "UID" + studentId;
    }

    @Override
    @Transactional
    public void confirmConsent(Long formId, Long studentId, String signature) {
        FirstVisitForm form = getById(formId);
        if (form == null || !form.getStudentId().equals(studentId)) {
            throw new BusinessException("登记表不存在");
        }
        if (!StringUtils.hasText(signature)) {
            throw new BusinessException("请填写电子签名（姓名）");
        }
        form.setHasReadConsent(1);
        form.setConsentTime(LocalDateTime.now());
        form.setConsentSignature(signature.trim());
        updateById(form);
    }

    @Override
    public FirstVisitForm getOwnedDetail(Long formId, Long studentId) {
        FirstVisitForm form = getById(formId);
        if (form == null || !form.getStudentId().equals(studentId)) {
            throw new BusinessException("无权查看此登记表");
        }
        return form;
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

    @Override
    public FirstVisitForm getLatestByStudentId(Long studentId) {
        return lambdaQuery()
                .eq(FirstVisitForm::getStudentId, studentId)
                .orderByDesc(FirstVisitForm::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<StudentSearchVO> searchByKeyword(String keyword) {
        List<FirstVisitForm> forms = lambdaQuery()
                .and(w -> w.like(FirstVisitForm::getStudentName, keyword)
                          .or()
                          .like(FirstVisitForm::getStudentNo, keyword))
                .orderByDesc(FirstVisitForm::getCreateTime)
                .list();

        // 按 studentId 去重，取最新
        List<StudentSearchVO> result = new ArrayList<>();
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (FirstVisitForm form : forms) {
            if (seen.add(form.getStudentId())) {
                StudentSearchVO vo = new StudentSearchVO();
                vo.setStudentId(form.getStudentId());
                vo.setStudentName(form.getStudentName());
                vo.setStudentNo(form.getStudentNo());
                vo.setDepartment(form.getDepartment());
                result.add(vo);
            }
        }
        return result;
    }
}
