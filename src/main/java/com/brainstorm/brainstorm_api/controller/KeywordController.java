package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.dto.KeywordRequest;
import com.brainstorm.brainstorm_api.entity.Keyword;
import com.brainstorm.brainstorm_api.service.KeywordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Keyword", description = "Keyword CRUD")
@RestController
@RequestMapping("/api/rooms/{roomId}/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    @GetMapping
    public List<Keyword> getKeywords(@PathVariable Long roomId) {
        return keywordService.getKeywordsByRoomId(roomId);
    }

    @PostMapping
    public ResponseEntity<Keyword> addKeyword(@PathVariable Long roomId, @RequestBody KeywordRequest keywordRequest) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Keyword keyword = keywordService.save(roomId, userId, keywordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(keyword);
    }


    @DeleteMapping("/{keywordId}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable Long keywordId) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        keywordService.delete(keywordId, userId);
        return ResponseEntity.noContent().build();
    }
}
