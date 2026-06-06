package org.example.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * 启用 common 模块中定义的 Feign 客户端
 */
@Configuration
@ConditionalOnClass(EnableFeignClients.class)
@EnableFeignClients(basePackages = "org.example.common.feign")
public class FeignClientConfig {
}
