package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.common.exception.KeywordFullException;
import com.brainstorm.brainstorm_api.common.exception.UnauthorizedAccessException;
import com.brainstorm.brainstorm_api.dto.KeywordRequest;
import com.brainstorm.brainstorm_api.entity.Keyword;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.KeywordRepository;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public List<Keyword> getKeywordsByRoomId(Long roomId) {
        return keywordRepository.findByRoomId(roomId);
    }

    public Keyword save(Long roomId, UUID userId, KeywordRequest keywordRequest) {
        final int MAX_KEYWORD = 10;
        long keywordCount = keywordRepository.countByRoomIdAndUserId(roomId, userId);
        if (keywordCount >= MAX_KEYWORD) {
            throw new KeywordFullException(String.format("Keyword Exceed (> %d)", MAX_KEYWORD));
        }
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NoSuchElementException("Not Found Room"));
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Not Found User"));

        Keyword keyword = new Keyword();
        keyword.setRoom(room);
        keyword.setUser(user);
        keyword.setContent(keywordRequest.getContent());

        return keywordRepository.save(keyword);
    }

    public void delete(Long keywordId, UUID userId) {
        Keyword keyword = keywordRepository.findById(keywordId).orElseThrow(() -> new NoSuchElementException("Not Found Keyword"));
        UUID keywordOwnerId = keyword.getUser().getId();
        UUID roomOwnerId = keyword.getRoom().getOwner().getId();
        if (!(userId.equals(keywordOwnerId) || userId.equals(roomOwnerId))) {
            throw new UnauthorizedAccessException("Access Denied");
        }

        keywordRepository.delete(keyword);
    }
}
