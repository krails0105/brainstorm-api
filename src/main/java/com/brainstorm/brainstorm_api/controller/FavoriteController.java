package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.entity.Favorite;
import com.brainstorm.brainstorm_api.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Favorite", description = "즐겨찾기 관리")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public Page<Favorite> listFavorite(@RequestParam(defaultValue = "0") int page) {
        UUID userId = (UUID) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        return favoriteService.getFavoriteByUserId(userId, pageable);
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<Favorite> createFavorite(@PathVariable Long roomId) {
        UUID userId = (UUID) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        Favorite save = favoriteService.save(userId, roomId);
        return ResponseEntity.status(HttpStatus.CREATED).body(save);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Long roomId) {
        UUID userId = (UUID) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        favoriteService.delete(userId, roomId);
        return ResponseEntity.noContent().build();
    }
}
