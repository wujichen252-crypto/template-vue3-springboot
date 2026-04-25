package com.template.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.template.common.result.ApiResult;
import com.template.common.result.RequestIdUtil;
import com.template.common.result.ResultCode;
import com.template.common.util.JwtUtil;
import com.template.service.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                if (tokenBlacklistService.isBlacklisted(token)) {
                    writeErrorResponse(response, ResultCode.UNAUTHORIZED);
                    return;
                }

                Claims claims = jwtUtil.parseToken(token);
                String type = (String) claims.get("type");
                if (!"access".equals(type)) {
                    writeErrorResponse(response, ResultCode.UNAUTHORIZED);
                    return;
                }

                Long userId = Long.valueOf(claims.getSubject());
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, ResultCode.UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void writeErrorResponse(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ApiResult<Void> result = ApiResult.error(resultCode.getCode(), resultCode.getMsg());
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
