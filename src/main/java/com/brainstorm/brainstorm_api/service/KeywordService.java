package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.common.exception.KeywordFullException;
import com.brainstorm.brainstorm_api.common.exception.UnauthorizedAccessException;
import com.brainstorm.brainstorm_api.dto.KeywordRequest;
import com.brainstorm.brainstorm_api.dto.KeywordResponse;
import com.brainstorm.brainstorm_api.entity.Keyword;
import com.brainstorm.brainstorm_api.entity.KeywordLike;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.KeywordLikeRepository;
import com.brainstorm.brainstorm_api.repository.KeywordRepository;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final KeywordLikeRepository keywordLikeRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public List<KeywordResponse> getKeywordsByRoomId(Long roomId, UUID userId) {
        List<Keyword> keywords = keywordRepository.findByRoomId(roomId);
        List<KeywordResponse> keywordResponses = new ArrayList<>();
        for (Keyword keyword: keywords) {
            KeywordResponse keywordResponse = new KeywordResponse();
            keywordResponse.setId(keyword.getId());
            keywordResponse.setNickname(keyword.getUser().getNickname());
            keywordResponse.setContent(keyword.getContent());
            keywordResponse.setLikeCount(keywordLikeRepository.countByKeywordId(keyword.getId()));
            keywordResponse.setLiked(keywordLikeRepository.existsByKeywordIdAndUserId(keyword.getId(), userId));
            keywordResponses.add(keywordResponse);
        }
        return keywordResponses;
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

    public KeywordResponse toggleLike(Long keywordId, UUID userId) {
        boolean exists = keywordLikeRepository.existsByKeywordIdAndUserId(keywordId, userId);
        Keyword keyword = keywordRepository.findById(keywordId).orElseThrow(() -> new NoSuchElementException("Not Found Keyword"));
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Not Found User"));

        KeywordResponse keywordResponse = new KeywordResponse();
        keywordResponse.setLiked(!exists);
        keywordResponse.setContent(keyword.getContent());
        keywordResponse.setNickname(user.getNickname());

        if (!exists) {
            KeywordLike like = new KeywordLike();
            like.setKeyword(keyword);
            like.setUser(user);
            keywordLikeRepository.save(like);
        } else {
            keywordLikeRepository.findByKeywordIdAndUserId(keywordId, userId).ifPresent(keywordLikeRepository::delete);
        }
        keywordResponse.setLikeCount(keywordLikeRepository.countByKeywordId(keywordId));


        return keywordResponse;
    }
}
