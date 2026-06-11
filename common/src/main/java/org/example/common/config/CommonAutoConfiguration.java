package org.example.common.config;

import org.example.common.config.FeignClientConfig;
import org.example.common.filter.UserContextFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * common 模块自动配置（使 @RestControllerAdvice 等注解在各服务中生效）
 */
@Configuration
@ComponentScan(basePackages = "org.example.common")
@Import(FeignClientConfig.class)
public class CommonAutoConfiguration {

    /**
     * 注册用户上下文过滤器（从网关转发的请求头提取用户信息到 ThreadLocal）
     */
    @Bean
    public FilterRegistrationBean<UserContextFilter> userContextFilterRegistration() {
        FilterRegistrationBean<UserContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserContextFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
