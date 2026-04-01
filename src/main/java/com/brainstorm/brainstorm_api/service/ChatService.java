package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.dto.ChatMessageResponse;
import com.brainstorm.brainstorm_api.entity.ChatMessage;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.ChatMessageRepository;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageResponse save(Long roomId, UUID userId, String content) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NoSuchElementException("Not Found Room"));
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Not Found User"));
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRoom(room);
        chatMessage.setUser(user);
        chatMessage.setContent(content);
        ChatMessage message = chatMessageRepository.save(chatMessage);
        return ChatMessageResponse.ofChatMessage(chatMessage);
    }

    public List<ChatMessageResponse> getChatMessagesByRoomId(Long roomId) {
        List<ChatMessage> chatMessages = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        return chatMessages.stream().map(ChatMessageResponse::ofChatMessage).toList();
    }
}
