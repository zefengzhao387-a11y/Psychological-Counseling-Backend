package org.example.common.feign;

import org.example.common.feign.dto.CounselorBriefDTO;
import org.example.common.feign.dto.UserBriefDTO;
import org.example.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(name = "user-service", url = "${service.user-url:http://localhost:8081}")
public interface UserFeignClient {

    @GetMapping("/api/v1/user/users/{id}")
    R<UserBriefDTO> getUserById(@PathVariable("id") Long id);

    /** 咨询师/初访员列表（服务间调用） */
    @GetMapping("/api/v1/user/counselor/list")
    R<List<CounselorBriefDTO>> listCounselors();
}
