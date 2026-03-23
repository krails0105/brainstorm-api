package com.brainstorm.brainstorm_api.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 모든 HTTP 요청마다 한 번씩 실행되는 JWT 인증 필터
// OncePerRequestFilter를 상속하면 요청당 정확히 1번만 실행됨
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더에서 JWT 토큰을 추출하고 검증하는 흐름:
        // 1. "Authorization" 헤더 가져오기 (없으면 null → Optional.empty)
        // 2. "Bearer "로 시작하는지 확인 (아니면 필터링됨)
        // 3. "Bearer " 이후 문자열(토큰)만 추출
        // 4. 토큰 서명/만료 검증 (무효하면 필터링됨)
        // 5. 토큰에서 userId(UUID) 추출
        // 6. SecurityContext에 인증 정보 저장
        //    → 이후 SecurityConfig의 authenticated()가 "인증된 요청"으로 인식
        //    → 토큰이 없거나 무효하면 SecurityContext가 비어있어서 403 반환
        Optional.ofNullable(request.getHeader("Authorization"))
            .filter(h -> h.startsWith("Bearer "))
            .map(h -> h.substring(7))
            .filter(jwtProvider::validateToken)
            .map(jwtProvider::getUserIdFromToken)
            .ifPresent(id -> {
                // UsernamePasswordAuthenticationToken(principal, credentials, authorities)
                // - principal: 인증된 유저 식별자 (userId) → Controller에서 getPrincipal()로 꺼냄
                // - credentials: 비밀번호 (이미 토큰으로 인증했으므로 null)
                // - authorities: 권한 목록 (현재는 빈 리스트, 역할 기반 권한이 필요하면 여기에 추가)
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(id, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });

        // 반드시 호출해야 함! 이 줄이 없으면 요청이 Controller까지 전달되지 않고 멈춤
        // 토큰 유무와 관계없이 항상 다음 필터로 넘겨야 함
        filterChain.doFilter(request, response);
    }
}
