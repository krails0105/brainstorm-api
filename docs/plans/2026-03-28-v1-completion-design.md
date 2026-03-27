# v1.0~1.2 완성 설계 문서

## 개요

플로우차트 기반으로 v1.0~1.2 마일스톤 완성에 필요한 나머지 기능 구현.
- RoomMember role (OWNER/MEMBER)
- Room 공유 링크 (share_token 생성, 토큰으로 입장)

## 1. RoomMember role 추가

### RoomMember 엔티티 변경
- `role` 필드 추가 (String: "OWNER" / "MEMBER")
- Room 생성 시 → role = "OWNER"
- 공유 링크/멤버 추가 시 → role = "MEMBER"

### 영향 범위
- RoomMemberService.save()에 role 파라미터 추가
- RoomService.save()에서 "OWNER"로 호출
- 기존 테스트 수정

## 2. Room 공유 링크

### Room 엔티티 변경
- `shareToken` 필드 추가 (String, unique)
- Room 생성 시 UUID.randomUUID()로 자동 생성

### API

| 메서드 | 경로 | 설명 | 권한 |
|--------|------|------|------|
| POST | `/api/rooms/{roomId}/share` | 공유 링크 생성 (재생성) | Owner만 |
| GET | `/api/share/{token}` | 토큰으로 룸 조회 + 멤버 자동 추가 | 인증된 유저 |

### 공유 링크 입장 흐름
1. FE: 공유 링크 클릭 → 로그인 여부 확인 (FE 처리)
2. 로그인 후 GET /api/share/{token} 호출
3. BE: 토큰으로 Room 조회 → 이미 멤버면 룸 정보 반환, 아니면 MEMBER로 추가 후 반환
4. 인원 초과 시 RoomFullException

### POST /api/rooms/{roomId}/share 응답
```json
{
  "shareUrl": "https://brainstorming-chat-sigma.vercel.app/join/{token}"
}
```

### GET /api/share/{token} 응답
```json
{
  "roomId": 1,
  "name": "Idea Room",
  "topic": "Startup ideas"
}
```

## 수정할 클래스

| 클래스 | 변경 내용 |
|--------|-----------|
| Room 엔티티 | shareToken 필드 추가 |
| RoomMember 엔티티 | role 필드 추가 |
| RoomMemberService | save에 role 파라미터 추가 |
| RoomService | 생성 시 shareToken 자동 생성, owner role "OWNER" |
| RoomController | POST /api/rooms/{roomId}/share 추가 |

## 새로 생성할 클래스

| 클래스 | 패키지 | 역할 |
|--------|--------|------|
| ShareController | controller | GET /api/share/{token} |
| ShareService | service | 토큰 조회 + 멤버 자동 추가 |
