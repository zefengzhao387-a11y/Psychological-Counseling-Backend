package org.example.common.support;

import lombok.extern.slf4j.Slf4j;
import org.example.common.feign.UserFeignClient;
import org.example.common.feign.dto.UserBriefDTO;
import org.example.common.result.R;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * 跨服务用户信息查询
 */
@Slf4j
@Component
@ConditionalOnBean(UserFeignClient.class)
public class UserLookupSupport {

    private final UserFeignClient userFeignClient;

    public UserLookupSupport(UserFeignClient userFeignClient) {
        this.userFeignClient = userFeignClient;
    }

    public UserBriefDTO getUser(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            R<UserBriefDTO> response = userFeignClient.getUserById(userId);
            if (response != null && response.getCode() == 200) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("查询用户 {} 失败: {}", userId, e.getMessage());
        }
        return null;
    }

    public String getPhone(Long userId) {
        UserBriefDTO user = getUser(userId);
        return user != null ? user.getPhone() : null;
    }

    public String getDisplayName(Long userId) {
        UserBriefDTO user = getUser(userId);
        if (user == null) {
            return "用户" + userId;
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return user.getUserNo() != null ? user.getUserNo() : "用户" + userId;
    }
}
