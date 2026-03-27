package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.common.ApiResponse;
import com.brainstorm.brainstorm_api.dto.KeywordRequest;
import com.brainstorm.brainstorm_api.dto.KeywordResponse;
import com.brainstorm.brainstorm_api.entity.Keyword;
import com.brainstorm.brainstorm_api.service.KeywordService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<List<KeywordResponse>>> getKeywords(@PathVariable Long roomId) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(keywordService.getKeywordsByRoomId(roomId, userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Keyword>> addKeyword(@PathVariable Long roomId, @Valid @RequestBody KeywordRequest keywordRequest) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Keyword keyword = keywordService.save(roomId, userId, keywordRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, keyword));
    }

    @DeleteMapping("/{keywordId}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable Long keywordId) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        keywordService.delete(keywordId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keywordId}/like")
    public ResponseEntity<ApiResponse<KeywordResponse>> addLike(@PathVariable Long keywordId) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KeywordResponse keywordResponse = keywordService.toggleLike(keywordId, userId);
        return ResponseEntity.ok(ApiResponse.success(keywordResponse));
    }
}
