package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.common.ApiResponse;
import com.brainstorm.brainstorm_api.dto.ChatMessageResponse;
import com.brainstorm.brainstorm_api.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Chat", description = """
    채팅 메시지 조회 (REST) 및 실시간 채팅 (WebSocket)

    ## WebSocket 실시간 채팅
    - 연결 엔드포인트: /ws (SockJS)
    - 구독(수신): /topic/rooms/{roomId}
    - 발행(전송): /app/rooms/{roomId}/chat
    - 발행 바디: {"userId": "UUID", "content": "메시지 내용"}
    - 응답 형식: {"id": 1, "nickname": "유저명", "content": "메시지", "createdAt": "2026-04-02T12:00:00"}
    """)
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @Operation(summary = "채팅 히스토리 조회", description = "룸의 이전 채팅 메시지를 시간순으로 조회")
    @GetMapping("/{roomId}/chat")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> listChatMessages(@PathVariable Long roomId) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ChatMessageResponse> messages = chatService.getChatMessagesByRoomId(roomId);
        return ResponseEntity.ok(ApiResponse.success(200, messages));
    }
}
