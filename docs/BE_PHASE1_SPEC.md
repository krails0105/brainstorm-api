# Brainstorm Web

## Backend Phase1 Development Specification

## 1. Overview

Phase1의 목표는 **사용자 인증과 브레인스토밍 룸 관리 기능 구현**이다.

사용자는 다음 기능을 수행할 수 있어야 한다.

1.  회원가입
2.  로그인
3.  브레인스토밍 룸 생성
4.  룸 이름 변경
5.  룸 주제 입력 및 수정
6.  룸 삭제
7.  룸 공유 링크 생성

Phase1에서는 **브레인스토밍 로직은 포함하지 않는다.**\
룸 관리와 협업 환경 준비만 구현한다.

------------------------------------------------------------------------

# 2. System Architecture

    Frontend (React / Vite)
            ↓ REST API
    Spring Boot Backend
            ↓
    PostgreSQL

Authentication 방식

    JWT Access Token

------------------------------------------------------------------------

# 3. Tech Stack

Backend Framework

    Spring Boot 3.x
    Java 17+

Libraries

    Spring Web
    Spring Data JPA
    Spring Security
    JWT
    Validation
    Lombok
    Springdoc OpenAPI (Swagger)

Database

    PostgreSQL

------------------------------------------------------------------------

# 4. Domain Model

## 4.1 User

사용자 정보 저장

### Fields

    id (UUID)
    email
    password
    nickname
    created_at
    updated_at

### Rules

    email must be unique
    password stored as bcrypt hash

------------------------------------------------------------------------

## 4.2 Room

브레인스토밍 방

### Fields

    id (UUID)
    name
    topic
    owner_id
    share_token
    created_at
    updated_at
    deleted_at

### Rules

    owner_id references User
    share_token used for invite link
    deleted_at used for soft delete

------------------------------------------------------------------------

## 4.3 RoomMember

룸 참여 사용자

### Fields

    id
    room_id
    user_id
    role
    created_at

### Role

    OWNER
    MEMBER

### Rules

    Room creator becomes OWNER

------------------------------------------------------------------------

# 5. API Specification

Base Path

    /api

------------------------------------------------------------------------

# 5.1 Authentication API

## Signup

    POST /api/auth/signup

### Request

``` json
{
  "email": "user@email.com",
  "password": "password",
  "nickname": "john"
}
```

### Response

``` json
{
  "userId": "uuid"
}
```

------------------------------------------------------------------------

## Login

    POST /api/auth/login

### Request

``` json
{
  "email": "user@email.com",
  "password": "password"
}
```

### Response

``` json
{
  "accessToken": "jwt",
  "user": {
    "id": "uuid",
    "nickname": "john"
  }
}
```

------------------------------------------------------------------------

# 5.2 Room API

Authentication required.

## Create Room

    POST /api/rooms

### Request

``` json
{
  "name": "Idea Room",
  "topic": "Startup ideas"
}
```

### Process

    1 verify user
    2 create room
    3 create room_member with role OWNER
    4 generate share_token

### Response

``` json
{
  "roomId": "uuid"
}
```

------------------------------------------------------------------------

## Get My Rooms

    GET /api/rooms

### Response

``` json
[
  {
    "id": "uuid",
    "name": "Idea Room",
    "topic": "Startup ideas"
  }
]
```

------------------------------------------------------------------------

## Get Room Detail

    GET /api/rooms/{roomId}

------------------------------------------------------------------------

## Update Room Name

    PATCH /api/rooms/{roomId}/name

### Request

``` json
{
  "name": "New Room Name"
}
```

### Authorization

    OWNER only

------------------------------------------------------------------------

## Update Room Topic

    PATCH /api/rooms/{roomId}/topic

### Request

``` json
{
  "topic": "AI startup ideas"
}
```

------------------------------------------------------------------------

## Delete Room

    DELETE /api/rooms/{roomId}

### Rules

    soft delete
    owner only

------------------------------------------------------------------------

# 5.3 Share Link

## Generate Share Link

    POST /api/rooms/{roomId}/share

### Response

``` json
{
  "shareUrl": "https://app.com/join/{token}"
}
```

### Rules

    token stored in Room.share_token

------------------------------------------------------------------------

## Get Room by Share Token

    GET /api/share/{token}

### Response

``` json
{
  "roomId": "uuid",
  "name": "Idea Room",
  "topic": "Startup ideas"
}
```

------------------------------------------------------------------------

# 6. Authentication

Authentication method

    JWT Access Token

Client must include

    Authorization: Bearer {token}

------------------------------------------------------------------------

# 7. Authorization

Room access rules

    Room update → OWNER only
    Room delete → OWNER only
    Room read → MEMBER allowed

Authorization based on

    RoomMember.role

------------------------------------------------------------------------

# 8. Package Structure

    com.brainstorm

    auth
      controller
      service
      dto

    user
      entity
      repository

    room
      controller
      service
      entity
      repository

    share
      controller
      service

    common
      config
      exception
      response

------------------------------------------------------------------------

# 9. Common Infrastructure

The backend must include

    JWT authentication filter
    Global exception handler
    API response wrapper
    Swagger documentation
    CORS configuration

------------------------------------------------------------------------

# 10. Development Milestone

## Week 1

    Project setup
    User entity
    Signup API
    Login API
    JWT authentication
    Room entity
    Create room API
    Get rooms API

## Week 2

    Update room APIs
    Delete room API
    Share link API
    Authorization checks
    Integration testing
    Swagger documentation
