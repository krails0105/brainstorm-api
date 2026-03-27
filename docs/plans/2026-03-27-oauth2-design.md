# OAuth2 소셜 로그인 설계 문서

## 개요

구글/카카오/네이버 소셜 로그인 지원. Spring OAuth2 Client 기반, BE 주도 흐름. 같은 이메일이면 기존 계정에 자동 연결.

## 로그인 흐름

```
1. FE: "구글로 로그인" 클릭 → BE /oauth2/authorization/google 리다이렉트
2. 구글 로그인 화면 → 유저 인증
3. 구글 → BE 콜백 URL로 인가 코드 전달
4. BE: 인가 코드로 구글에서 유저 정보(이메일, 이름) 받음
5. BE: 이메일로 DB 조회 → 없으면 신규 생성, 있으면 기존 계정 연결
6. BE: JWT 발급 → FE로 리다이렉트 (쿼리 파라미터에 토큰 포함)
```

## User 엔티티 변경

| 추가 필드 | 타입 | 설명 |
|-----------|------|------|
| provider | String | "LOCAL", "GOOGLE", "KAKAO", "NAVER" |
| providerId | String | OAuth 제공자의 유저 고유 ID |

## 계정 연결 로직

- OAuth 로그인 시 이메일로 DB 조회
- 기존 유저 있음 → provider/providerId 업데이트 → JWT 발급
- 기존 유저 없음 → 신규 User 생성 (password null) → JWT 발급

## 새로 생성할 클래스

| 클래스 | 패키지 | 역할 |
|--------|--------|------|
| CustomOAuth2UserService | config.oauth | OAuth2 로그인 시 유저 정보 처리 (조회/생성/연결) |
| OAuth2SuccessHandler | config.oauth | 로그인 성공 후 JWT 발급 + FE 리다이렉트 |

## 수정할 클래스

| 클래스 | 변경 내용 |
|--------|-----------|
| User 엔티티 | provider, providerId 필드 추가 |
| SecurityConfig | .oauth2Login() 설정 추가 |
| build.gradle | spring-boot-starter-oauth2-client 의존성 추가 |
| application.properties | 구글/카카오/네이버 client-id, client-secret |

## OAuth2 제공자별 설정

### Google
- 등록: https://console.cloud.google.com
- scope: email, profile

### Kakao
- 등록: https://developers.kakao.com
- scope: account_email, profile_nickname

### Naver
- 등록: https://developers.naver.com
- scope: email, name
