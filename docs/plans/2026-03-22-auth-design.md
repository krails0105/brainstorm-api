# Auth 구현 계획

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** JWT Access Token 기반 회원가입/로그인 인증 구현

**Architecture:** Spring Security + JWT 필터 체인. 회원가입 시 bcrypt 암호화, 로그인 시 JWT 발급, 이후 요청은 JwtFilter가 토큰 검증 후 SecurityContext에 인증 정보 설정.

**Tech Stack:** Spring Boot 3.x, Spring Security, jjwt 0.12.x, BCryptPasswordEncoder, H2/PostgreSQL

---

### Task 1: build.gradle에 JWT 의존성 추가

**Files:**
- Modify: `build.gradle:26-39`

**Step 1: jjwt 의존성 추가**

```gradle
// 기존 dependencies 블록에 추가
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
```

**Step 2: 빌드 확인**

Run: `./gradlew dependencies --configuration runtimeClasspath | grep jjwt`
Expected: jjwt-api, jjwt-impl, jjwt-jackson 출력

**Step 3: Commit**

```bash
git add build.gradle
git commit -m "chore: jjwt 의존성 추가"
```

---

### Task 2: application.properties에 JWT 설정 추가

**Files:**
- Modify: `src/main/resources/application.properties`

**Step 1: JWT 설정 추가**

```properties
# JWT
jwt.secret=brainstorm-dev-secret-key-must-be-at-least-256-bits-long-for-hs256
jwt.expiration=604800000
```

> 604800000ms = 7일. secret은 개발용이며 prod에서는 환경변수로 관리.

**Step 2: Commit**

```bash
git add src/main/resources/application.properties
git commit -m "chore: JWT secret, expiration 설정 추가"
```

---

### Task 3: User 엔티티 수정 (id → UUID, email/nickname 추가)

**Files:**
- Modify: `src/main/java/com/brainstorm/brainstorm_api/entity/User.java`
- Modify: `src/main/java/com/brainstorm/brainstorm_api/repository/UserRepository.java`

**Step 1: User 엔티티 수정**

```java
package com.brainstorm.brainstorm_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 로그인에 사용되는 이메일 (중복 불가)
    @Column(unique = true, nullable = false)
    private String email;

    // bcrypt로 암호화된 비밀번호
    @Column(nullable = false)
    private String password;

    // 화면에 표시되는 닉네임
    @Column(nullable = false)
    private String nickname;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

**Step 2: UserRepository 수정**

```java
package com.brainstorm.brainstorm_api.repository;

import com.brainstorm.brainstorm_api.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // 로그인 시 이메일로 유저를 찾기 위한 메서드
    Optional<User> findByEmail(String email);

    // 회원가입 시 이메일 중복 확인용 메서드
    boolean existsByEmail(String email);
}
```

**Step 3: 연관 엔티티 수정 — Room, RoomMember, Favorite**

Room, RoomMember, Favorite 엔티티는 `@ManyToOne`으로 User를 참조하므로 User의 id 타입이 바뀌면 JPA가 자동으로 처리한다. 별도 수정 불필요.

단, RoomService, RoomMemberService, FavoriteService에서 `User.getId()` 리턴 타입이 `Long → UUID`로 바뀌므로 **기존 서비스/테스트에서 Long으로 받는 곳**을 UUID로 수정해야 한다.

**Step 4: 기존 Service/Controller/Test에서 userId 타입 Long → UUID 수정**

영향 받는 파일 확인:
- `RoomMemberService.java` — `save(Long roomId, Long userId)` → userId를 UUID로
- `RoomMemberController.java` — PathVariable 타입
- `FavoriteController.java` — PathVariable 타입
- `FavoriteService.java` — userId 파라미터
- `RoomServiceTest.java` — user.getId() 사용부
- `RoomMemberServiceTest.java` — user.getId() 사용부
- `FavoriteServiceTest.java` — user.getId() 사용부

> 각 파일에서 `Long userId` → `UUID userId`로 타입만 변경하면 됨.

**Step 5: 빌드 및 테스트**

Run: `./gradlew test`
Expected: 기존 15개 테스트 PASS

**Step 6: Commit**

```bash
git add src/main/java/com/brainstorm/brainstorm_api/entity/User.java
git add src/main/java/com/brainstorm/brainstorm_api/repository/UserRepository.java
git add -A  # 변경된 서비스/컨트롤러/테스트
git commit -m "refactor: User id를 UUID로 변경, email/nickname 필드 추가"
```

---

### Task 4: Auth DTO 생성

**Files:**
- Create: `src/main/java/com/brainstorm/brainstorm_api/dto/SignupRequest.java`
- Create: `src/main/java/com/brainstorm/brainstorm_api/dto/SignupResponse.java`
- Create: `src/main/java/com/brainstorm/brainstorm_api/dto/LoginRequest.java`
- Create: `src/main/java/com/brainstorm/brainstorm_api/dto/LoginResponse.java`

**Step 1: SignupRequest**

```java
package com.brainstorm.brainstorm_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String email;
    private String password;
    private String nickname;
}
```

**Step 2: SignupResponse**

```java
package com.brainstorm.brainstorm_api.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {
    private UUID userId;
}
```

**Step 3: LoginRequest**

```java
package com.brainstorm.brainstorm_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
}
```

**Step 4: LoginResponse**

```java
package com.brainstorm.brainstorm_api.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private UserInfo user;

    @Getter
    @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String nickname;
    }
}
```

**Step 5: Commit**

```bash
git add src/main/java/com/brainstorm/brainstorm_api/dto/
git commit -m "feat: Auth 관련 DTO 추가 (Signup, Login)"
```

---

### Task 5: JwtProvider 구현

**Files:**
- Create: `src/main/java/com/brainstorm/brainstorm_api/config/jwt/JwtProvider.java`

**Step 1: JwtProvider 작성**

```java
package com.brainstorm.brainstorm_api.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// JWT 토큰 생성과 검증을 담당하는 클래스
@Component
public class JwtProvider {

    private final SecretKey key;
    private final long expiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        // secret 문자열로 HMAC-SHA 서명 키 생성
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    // 유저 ID를 담은 JWT 토큰 생성
    public String createToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // 토큰에서 유저 ID 추출
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }

    // 토큰 유효성 검증 (서명 + 만료시간)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Step 2: 테스트 작성**

Create: `src/test/java/com/brainstorm/brainstorm_api/config/jwt/JwtProviderTest.java`

```java
package com.brainstorm.brainstorm_api.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void 토큰_생성_및_검증() {
        UUID userId = UUID.randomUUID();

        String token = jwtProvider.createToken(userId);

        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    void 잘못된_토큰은_검증_실패() {
        assertThat(jwtProvider.validateToken("invalid.token.here")).isFalse();
    }
}
```

**Step 3: 테스트 실행**

Run: `./gradlew test --tests "*JwtProviderTest"`
Expected: 2개 테스트 PASS

**Step 4: Commit**

```bash
git add src/main/java/com/brainstorm/brainstorm_api/config/jwt/JwtProvider.java
git add src/test/java/com/brainstorm/brainstorm_api/config/jwt/JwtProviderTest.java
git commit -m "feat: JwtProvider 구현 및 테스트"
```

---

### Task 6: Auth 예외 클래스 추가

**Files:**
- Create: `src/main/java/com/brainstorm/brainstorm_api/common/exception/DuplicateEmailException.java`
- Create: `src/main/java/com/brainstorm/brainstorm_api/common/exception/InvalidCredentialsException.java`
- Modify: `src/main/java/com/brainstorm/brainstorm_api/common/exception/GlobalExceptionHandler.java`

**Step 1: DuplicateEmailException**

```java
package com.brainstorm.brainstorm_api.common.exception;

// 회원가입 시 이미 존재하는 이메일로 가입하려 할 때 발생
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
```

**Step 2: InvalidCredentialsException**

```java
package com.brainstorm.brainstorm_api.common.exception;

// 로그인 시 이메일 또는 비밀번호가 틀렸을 때 발생
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
```

**Step 3: GlobalExceptionHandler에 핸들러 추가**

```java
// 기존 핸들러들 아래에 추가

@ExceptionHandler(DuplicateEmailException.class)
public ResponseEntity<String> handleDuplicateEmail(DuplicateEmailException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
}

@ExceptionHandler(InvalidCredentialsException.class)
public ResponseEntity<String> handleInvalidCredentials(InvalidCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
}
```

**Step 4: Commit**

```bash
git add src/main/java/com/brainstorm/brainstorm_api/common/exception/
git commit -m "feat: Auth 예외 클래스 추가 (DuplicateEmail, InvalidCredentials)"
```

---

### Task 7: AuthService 구현

**Files:**
- Create: `src/main/java/com/brainstorm/brainstorm_api/service/AuthService.java`

**Step 1: AuthService 작성**

```java
package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.common.exception.DuplicateEmailException;
import com.brainstorm.brainstorm_api.common.exception.InvalidCredentialsException;
import com.brainstorm.brainstorm_api.config.jwt.JwtProvider;
import com.brainstorm.brainstorm_api.dto.LoginRequest;
import com.brainstorm.brainstorm_api.dto.LoginResponse;
import com.brainstorm.brainstorm_api.dto.SignupRequest;
import com.brainstorm.brainstorm_api.dto.SignupResponse;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 회원가입: 이메일 중복 확인 → 비밀번호 암호화 → 저장
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());

        User saved = userRepository.save(user);
        return new SignupResponse(saved.getId());
    }

    // 로그인: 이메일로 유저 조회 → 비밀번호 검증 → JWT 발급
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtProvider.createToken(user.getId());
        return new LoginResponse(token, new LoginResponse.UserInfo(user.getId(), user.getNickname()));
    }
}
```

**Step 2: 테스트 작성**

Create: `src/test/java/com/brainstorm/brainstorm_api/service/AuthServiceTest.java`

```java
package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.common.exception.DuplicateEmailException;
import com.brainstorm.brainstorm_api.common.exception.InvalidCredentialsException;
import com.brainstorm.brainstorm_api.dto.LoginRequest;
import com.brainstorm.brainstorm_api.dto.LoginResponse;
import com.brainstorm.brainstorm_api.dto.SignupRequest;
import com.brainstorm.brainstorm_api.dto.SignupResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void 회원가입_성공() {
        SignupRequest request = new SignupRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setNickname("테스터");

        SignupResponse response = authService.signup(request);

        assertThat(response.getUserId()).isNotNull();
    }

    @Test
    void 이메일_중복_회원가입_실패() {
        SignupRequest request = new SignupRequest();
        request.setEmail("dup@example.com");
        request.setPassword("password123");
        request.setNickname("테스터");
        authService.signup(request);

        SignupRequest duplicate = new SignupRequest();
        duplicate.setEmail("dup@example.com");
        duplicate.setPassword("password456");
        duplicate.setNickname("테스터2");

        assertThatThrownBy(() -> authService.signup(duplicate))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void 로그인_성공() {
        SignupRequest signup = new SignupRequest();
        signup.setEmail("login@example.com");
        signup.setPassword("password123");
        signup.setNickname("로그인유저");
        authService.signup(signup);

        LoginRequest login = new LoginRequest();
        login.setEmail("login@example.com");
        login.setPassword("password123");

        LoginResponse response = authService.login(login);

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getUser().getNickname()).isEqualTo("로그인유저");
    }

    @Test
    void 잘못된_비밀번호_로그인_실패() {
        SignupRequest signup = new SignupRequest();
        signup.setEmail("wrong@example.com");
        signup.setPassword("password123");
        signup.setNickname("유저");
        authService.signup(signup);

        LoginRequest login = new LoginRequest();
        login.setEmail("wrong@example.com");
        login.setPassword("wrongpassword");

        assertThatThrownBy(() -> authService.login(login))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void 존재하지_않는_이메일_로그인_실패() {
        LoginRequest login = new LoginRequest();
        login.setEmail("noone@example.com");
        login.setPassword("password123");

        assertThatThrownBy(() -> authService.login(login))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
```

**Step 3: 테스트 실행**

Run: `./gradlew test --tests "*AuthServiceTest"`
Expected: 5개 테스트 PASS

**Step 4: Commit**

```bash
git add src/main/java/com/brainstorm/brainstorm_api/service/AuthService.java
git add src/test/java/com/brainstorm/brainstorm_api/service/AuthServiceTest.java
git commit -m "feat: AuthService 구현 및 테스트 (회원가입/로그인)"
```

---

### Task 8: AuthController 구현

**Files:**
- Create: `src/main/java/com/brainstorm/brainstorm_api/controller/AuthController.java`

**Step 1: AuthController 작성**

```java
package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.dto.LoginRequest;
import com.brainstorm.brainstorm_api.dto.LoginResponse;
import com.brainstorm.brainstorm_api.dto.SignupRequest;
import com.brainstorm.brainstorm_api.dto.SignupResponse;
import com.brainstorm.brainstorm_api.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입/로그인")
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 로그인 API — 성공 시 JWT 토큰 반환
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/brainstorm/brainstorm_api/controller/AuthController.java
git commit -m "feat: AuthController 구현 (회원가입/로그인 API)"
```

---

### Task 9: JwtFilter + SecurityConfig 수정

**Files:**
- Create: `src/main/java/com/brainstorm/brainstorm_api/config/jwt/JwtFilter.java`
- Modify: `src/main/java/com/brainstorm/brainstorm_api/config/SecurityConfig.java`

**Step 1: JwtFilter 작성**

```java
package com.brainstorm.brainstorm_api.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// 매 요청마다 Authorization 헤더에서 JWT를 꺼내 검증하는 필터
@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // "Bearer {token}" 형식에서 토큰 추출
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtProvider.validateToken(token)) {
                UUID userId = jwtProvider.getUserIdFromToken(token);

                // SecurityContext에 인증 정보 설정 (권한은 빈 리스트)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**Step 2: SecurityConfig 수정**

```java
package com.brainstorm.brainstorm_api.config;

import com.brainstorm.brainstorm_api.config.jwt.JwtFilter;
import lombok.AllArgsConstructor;
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
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            // JWT는 Stateless이므로 세션을 사용하지 않음
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 인증 불필요 경로
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // 나머지는 인증 필요
                .anyRequest().authenticated())
            // JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 비밀번호 암호화에 사용하는 BCrypt 인코더
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Step 3: 전체 테스트 실행**

Run: `./gradlew test`
Expected: 모든 테스트 PASS (기존 15개 + JwtProvider 2개 + AuthService 5개)

**Step 4: Commit**

```bash
git add src/main/java/com/brainstorm/brainstorm_api/config/jwt/JwtFilter.java
git add src/main/java/com/brainstorm/brainstorm_api/config/SecurityConfig.java
git commit -m "feat: JwtFilter 구현 및 SecurityConfig에 인증 규칙 적용"
```

---

### Task 10: 수동 통합 테스트

**Step 1: 서버 실행**

Run: `./gradlew bootRun`

**Step 2: Swagger에서 테스트**

1. `http://localhost:8080/docs` 접속
2. `POST /api/auth/signup` — 회원가입
3. `POST /api/auth/login` — 로그인 → accessToken 복사
4. `GET /api/rooms` — 토큰 없이 요청 → 401/403 확인
5. `GET /api/rooms` — Authorization: Bearer {token} 헤더 추가 → 200 확인

**Step 3: 최종 Commit**

```bash
git commit -m "feat: Auth(JWT) 구현 완료 — 회원가입, 로그인, 토큰 인증"
```
