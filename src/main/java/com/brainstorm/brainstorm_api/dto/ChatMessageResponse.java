package com.brainstorm.brainstorm_api.dto;

import com.brainstorm.brainstorm_api.entity.ChatMessage;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageResponse {

    private Long id;

    private String nickname;

    private String content;

    private LocalDateTime createdAt;

    public static ChatMessageResponse ofChatMessage(ChatMessage chatMessage) {
        ChatMessageResponse messageResponse = new ChatMessageResponse();
        messageResponse.setId(chatMessage.getId());
        messageResponse.setNickname(chatMessage.getUser().getNickname());
        messageResponse.setContent(chatMessage.getContent());
        messageResponse.setCreatedAt(chatMessage.getCreatedAt());
        return messageResponse;
    }

}
