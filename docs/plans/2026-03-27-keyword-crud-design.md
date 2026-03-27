# 키워드 CRUD 설계 문서

## 개요

Room 내에서 수동으로 키워드(단어)를 생성/조회/삭제하는 기능. 1인당 룸별 최대 10개 제한.

## 엔티티

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| room | Room (ManyToOne) | 어떤 룸의 키워드 |
| user | User (ManyToOne) | 누가 만들었는지 |
| content | String (not null) | 키워드 단어 |
| createdAt | LocalDateTime | 생성일 |

유니크 제약: `(room_id, user_id, content)` — 같은 룸에서 같은 유저가 같은 키워드 중복 방지

## API

| 메서드 | 경로 | 설명 | 권한 |
|--------|------|------|------|
| GET | `/api/rooms/{roomId}/keywords` | 룸의 키워드 목록 조회 | 인증된 유저 |
| POST | `/api/rooms/{roomId}/keywords` | 키워드 생성 (1인당 최대 10개) | 인증된 유저 |
| DELETE | `/api/rooms/{roomId}/keywords/{keywordId}` | 키워드 삭제 | 본인 또는 Room owner |

### POST /api/rooms/{roomId}/keywords

**Request:**
```json
{
  "content": "자동화"
}
```

**Response (201):**
```json
{
  "id": 1,
  "content": "자동화",
  "user": { "id": "...", "nickname": "홍길동" },
  "createdAt": "2026-03-27T10:00:00"
}
```

## 새로 생성할 클래스

| 클래스 | 패키지 | 역할 |
|--------|--------|------|
| Keyword | entity | 키워드 엔티티 |
| KeywordRepository | repository | DB 접근 |
| KeywordService | service | 비즈니스 로직 (10개 제한, 삭제 권한 체크) |
| KeywordController | controller | API 엔드포인트 |
| KeywordRequest | dto | 생성 요청 DTO (content만) |

## 검증 로직

- 생성 시: 해당 룸에서 본인 키워드 10개 초과 → 예외
- 삭제 시: 본인 키워드이거나 Room owner인지 확인 → 아니면 403
