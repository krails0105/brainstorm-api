package com.brainstorm.brainstorm_api.config;

import com.brainstorm.brainstorm_api.config.jwt.JwtFilter;
import com.brainstorm.brainstorm_api.config.oauth.OAuth2SuccessHandler;
import com.brainstorm.brainstorm_api.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Spring Security 설정을 직접 커스터마이징하겠다는 선언
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF(Cross-Site Request Forgery) 비활성화
            // CSRF = 악성 사이트가 사용자의 브라우저 쿠키를 이용해 우리 서버에 위조 요청을 보내는 공격
            // 쿠키 기반 인증에서만 위험하고, JWT는 헤더로 전달하므로 CSRF 방어 불필요
            .csrf(csrf -> csrf.disable())
            // CORS 활성화 — WebConfig에 설정한 CORS 규칙(허용 도메인, 메서드 등)을 Security에도 적용
            .cors(Customizer.withDefaults())
            // 세션 사용 안 함 — JWT는 Stateless 인증이므로 서버에 세션을 저장하지 않음
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            // 경로별 인증 규칙 설정
            .authorizeHttpRequests(auth -> auth
                // 회원가입/로그인은 토큰 없이 접근 가능 (인증 전이니까)
                .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()
                // Swagger 문서 경로도 인증 없이 접근 가능
                .requestMatchers("/docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers( "/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/chat-test.html").permitAll()
                // H2 콘솔 (개발용 DB 관리 화면)
                // Actuator 헬스체크 — Railway가 앱 상태를 확인하는 엔드포인트
                .requestMatchers("/actuator/health").permitAll()
                // H2 콘솔 (개발용 DB 관리 화면)
                .requestMatchers("/h2-console/**").permitAll()
                // 위에서 명시하지 않은 나머지 모든 경로는 인증 필요
                // → SecurityContext에 authentication이 없으면 403 반환
                .anyRequest().authenticated())
            .oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler))
            // JwtFilter를 Spring Security 기본 인증 필터(UsernamePasswordAuthenticationFilter) 앞에 추가
            // → 요청이 들어오면 JwtFilter가 먼저 토큰을 검증하고 SecurityContext에 인증 정보를 설정
            // → 그 다음 Spring Security가 인증 여부를 확인
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 비밀번호 암호화에 사용하는 BCrypt 인코더
    // - 회원가입 시: passwordEncoder.encode("평문") → 암호화된 문자열 저장
    // - 로그인 시: passwordEncoder.matches("입력값", "저장된 암호화 값") → true/false
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
