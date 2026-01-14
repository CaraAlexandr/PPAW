package com.ppaw.passwordvault.config;

import com.ppaw.passwordvault.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * LAB 8: Authentication interceptor to extract userId from token
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Allow OPTIONS requests for CORS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        // Skip authentication for login and public endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/health") || 
            path.startsWith("/swagger-ui") || path.startsWith("/api-docs") ||
            path.equals("/error")) {
            return true;
        }

        // Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        Long userId = tokenService.validateToken(token);

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // Store userId in request attribute for use in controllers
        request.setAttribute("userId", userId);
        return true;
    }
}

