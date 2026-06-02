package org.example.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.example.common.context.UserContext;

import java.io.IOException;

/**
 * 用户上下文过滤器（从网关转发的请求头中提取用户信息）
 */
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String userId = httpRequest.getHeader("X-User-Id");
        String username = httpRequest.getHeader("X-Username");
        String roleCode = httpRequest.getHeader("X-Role-Code");

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
