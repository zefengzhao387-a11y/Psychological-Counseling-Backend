package org.example.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.example.common.context.UserContext;
import org.example.common.utils.JwtUtil;

import java.io.IOException;

/**
 * 用户上下文过滤器（从网关转发的请求头或 JWT 中提取用户信息）
 */
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String userId = httpRequest.getHeader("X-User-Id");
        String username = httpRequest.getHeader("X-Username");
        String roleCode = httpRequest.getHeader("X-Role-Code");

        // 本地开发时 Vite 直连微服务，不会经过网关注入请求头，改从 Authorization 解析 JWT
        if (userId == null) {
            String auth = httpRequest.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                if (JwtUtil.validate(token)) {
                    userId = String.valueOf(JwtUtil.getUserId(token));
                    if (username == null) {
                        username = JwtUtil.getUsername(token);
                    }
                    if (roleCode == null) {
                        roleCode = String.valueOf(JwtUtil.getRoleCode(token));
                    }
                }
            }
        }

        if (userId != null) {
            UserContext.setUserId(Long.valueOf(userId));
        }
        if (username != null) {
            UserContext.setUsername(username);
        }
        if (roleCode != null) {
            UserContext.setRoleCode(Integer.valueOf(roleCode));
        }

        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
