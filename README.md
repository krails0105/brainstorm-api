# Brainstorm API

브레인스토밍 협업 웹 서비스의 백엔드 API

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- OAuth2 (Google, Kakao, Naver)
- H2 (dev) / PostgreSQL (prod)
- Swagger (springdoc-openapi)
- Lombok

## Getting Started

### 개발 환경 실행

```bash
./gradlew bootRun
```

- 서버: http://localhost:8080
- Swagger: http://localhost:8080/docs
- H2 Console: http://localhost:8080/h2-console

### 테스트

```bash
./gradlew test
```

### 프로덕션 실행 (Railway)

```bash
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

## API Endpoints

### Auth

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/auth/signup` | 회원가입 | X |
| POST | `/api/auth/login` | 로그인 (JWT 발급) | X |
| GET | `/api/auth/status` | 토큰 유효성 확인 | O |

### OAuth2

| Path | Description |
|------|-------------|
| `/oauth2/authorization/google` | 구글 로그인 |
| `/oauth2/authorization/kakao` | 카카오 로그인 |
| `/oauth2/authorization/naver` | 네이버 로그인 |

### Room

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/rooms` | 룸 목록 조회 (페이징) | O |
| GET | `/api/rooms/{id}` | 룸 상세 조회 | O |
| POST | `/api/rooms` | 룸 생성 | O |
| PUT | `/api/rooms/{id}` | 룸 수정 (owner만) | O |
| DELETE | `/api/rooms/{id}` | 룸 삭제 (owner만) | O |
| POST | `/api/rooms/{id}/share` | 공유 링크 생성 (owner만) | O |

### Share

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/share/{token}` | 공유 링크로 룸 입장 | O |

### Room Member

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/rooms/{roomId}/members` | 멤버 목록 조회 | O |
| POST | `/api/rooms/{roomId}/members/{userId}` | 멤버 추가 | O |
| DELETE | `/api/rooms/{roomId}/members/{userId}` | 멤버 삭제 | O |

### Keyword

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/rooms/{roomId}/keywords` | 키워드 목록 조회 (좋아요 정보 포함) | O |
| POST | `/api/rooms/{roomId}/keywords` | 키워드 생성 (1인당 10개) | O |
| DELETE | `/api/rooms/{roomId}/keywords/{keywordId}` | 키워드 삭제 (본인 or owner) | O |
| POST | `/api/rooms/{roomId}/keywords/{keywordId}/like` | 좋아요 토글 | O |

### Favorite

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/api/favorites` | 내 즐겨찾기 목록 | O |
| POST | `/api/favorites/{roomId}` | 즐겨찾기 추가 | O |
| DELETE | `/api/favorites/{roomId}` | 즐겨찾기 삭제 | O |

## API Response Format

### Success

```json
{
  "status": 200,
  "data": { ... }
}
```

### Error

```json
{
  "status": 400,
  "error": "error message"
}
```

## Project Structure

```
src/main/java/com/brainstorm/brainstorm_api/
├── common/
│   ├── ApiResponse.java
│   ├── RoomRole.java
│   └── exception/
├── config/
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   ├── jwt/
│   │   ├── JwtProvider.java
│   │   └── JwtFilter.java
│   └── oauth/
│       ├── OAuth2UserInfo.java
│       ├── GoogleOAuth2UserInfo.java
│       ├── KakaoOAuth2UserInfo.java
│       ├── NaverOAuth2UserInfo.java
│       └── OAuth2SuccessHandler.java
├── controller/
├── dto/
├── entity/
├── repository/
└── service/
```

## Environment Variables (Production)

| Variable | Description |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | PostgreSQL URL |
| `JWT_SECRET` | JWT 서명 키 (32자 이상) |
| `FRONTEND_URL` | 프론트엔드 URL |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret |
| `KAKAO_CLIENT_ID` | Kakao OAuth client ID |
| `KAKAO_CLIENT_SECRET` | Kakao OAuth client secret |
| `NAVER_CLIENT_ID` | Naver OAuth client ID |
| `NAVER_CLIENT_SECRET` | Naver OAuth client secret |
