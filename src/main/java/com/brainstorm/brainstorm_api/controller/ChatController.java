package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.dto.ChatMessageRequest;
import com.brainstorm.brainstorm_api.dto.ChatMessageResponse;
import com.brainstorm.brainstorm_api.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/rooms/{roomId}/chat")
    @SendTo("/topic/rooms/{roomId}")
    public ChatMessageResponse sendChatMessages(@DestinationVariable Long roomId, ChatMessageRequest chatMessageRequest) {
        return chatService.save(roomId, chatMessageRequest.getUserId(), chatMessageRequest.getContent());
    }
}
