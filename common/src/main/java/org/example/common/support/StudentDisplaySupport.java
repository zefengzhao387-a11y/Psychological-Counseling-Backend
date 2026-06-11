package org.example.common.support;

import lombok.extern.slf4j.Slf4j;
import org.example.common.feign.AppointmentFeignClient;
import org.example.common.feign.UserFeignClient;
import org.example.common.feign.dto.StudentProfileBriefDTO;
import org.example.common.feign.dto.UserBriefDTO;
import org.example.common.result.R;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 学生展示信息解析（优先用户服务，失败则回退首访登记表）
 */
@Slf4j
@Component
@ConditionalOnBean(UserFeignClient.class)
public class StudentDisplaySupport {

    private final UserFeignClient userFeignClient;
    private final ObjectProvider<AppointmentFeignClient> appointmentFeignClientProvider;

    public StudentDisplaySupport(UserFeignClient userFeignClient,
                                 ObjectProvider<AppointmentFeignClient> appointmentFeignClientProvider) {
        this.userFeignClient = userFeignClient;
        this.appointmentFeignClientProvider = appointmentFeignClientProvider;
    }

    public StudentProfileBriefDTO resolve(Long studentId) {
        StudentProfileBriefDTO profile = new StudentProfileBriefDTO();
        profile.setStudentId(studentId);
        if (studentId == null) {
            profile.setStudentName("-");
            return profile;
        }

        UserBriefDTO user = fetchUser(studentId);
        if (user != null) {
            profile.setStudentName(StringUtils.hasText(user.getUsername()) ? user.getUsername() : user.getUserNo());
            profile.setStudentNo(user.getUserNo());
            if (StringUtils.hasText(profile.getStudentName())) {
                return profile;
            }
        }

        StudentProfileBriefDTO fromForm = fetchFromForm(studentId);
        if (fromForm != null && StringUtils.hasText(fromForm.getStudentName())) {
            return fromForm;
        }

        profile.setStudentName("用户" + studentId);
        return profile;
    }

    private UserBriefDTO fetchUser(Long studentId) {
        try {
            R<UserBriefDTO> response = userFeignClient.getUserById(studentId);
            if (response != null && response.getCode() == 200) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("用户服务查询学生 {} 失败: {}", studentId, e.getMessage());
        }
        return null;
    }

    private StudentProfileBriefDTO fetchFromForm(Long studentId) {
        AppointmentFeignClient client = appointmentFeignClientProvider.getIfAvailable();
        if (client == null) {
            return null;
        }
        try {
            R<StudentProfileBriefDTO> response = client.getStudentProfile(studentId);
            if (response != null && response.getCode() == 200) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("登记表查询学生 {} 失败: {}", studentId, e.getMessage());
        }
        return null;
    }
}
