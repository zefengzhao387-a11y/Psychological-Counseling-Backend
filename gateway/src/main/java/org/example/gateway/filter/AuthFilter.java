package org.example.gateway.filter;

import org.example.common.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 网关全局认证过滤器
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    /** 白名单路径（无需登录） */
    private static final List<String> WHITELIST = List.of(
            "/api/v1/user/auth/login",
            "/api/v1/user/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单放行
        for (String white : WHITELIST) {
            if (path.startsWith(white)) {
                return chain.filter(exchange);
            }
        }

        // 获取 token
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        token = token.substring(7);

        // 校验 token
        if (!JwtUtil.validate(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 将用户信息写入请求头，转发给下游服务
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(JwtUtil.getUserId(token)))
                .header("X-Username", JwtUtil.getUsername(token))
                .header("X-Role-Code", String.valueOf(JwtUtil.getRoleCode(token)))
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
