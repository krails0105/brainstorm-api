package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.common.ApiResponse;
import com.brainstorm.brainstorm_api.dto.ChatMessageResponse;
import com.brainstorm.brainstorm_api.service.ChatService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/{roomId}/chat")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> listChatMessages(@PathVariable Long roomId) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ChatMessageResponse> messages = chatService.getChatMessagesByRoomId(roomId);
        return ResponseEntity.ok(ApiResponse.success(200, messages));
    }
}
