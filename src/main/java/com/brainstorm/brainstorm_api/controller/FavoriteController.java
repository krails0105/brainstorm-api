package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.entity.Favorite;
import com.brainstorm.brainstorm_api.service.FavoriteService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/favorites")
@AllArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public Page<Favorite> listFavorite(@PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page) {

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        return favoriteService.getFavoriteByUserId(userId, pageable);
    }

    @PostMapping("/{roomId}")
    public ResponseEntity<Favorite> createFavorite(@PathVariable Long userId,
        @PathVariable Long roomId) {
        Favorite save = favoriteService.save(userId, roomId);
        return ResponseEntity.status(HttpStatus.CREATED).body(save);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Long userId,
        @PathVariable Long roomId) {
        favoriteService.delete(userId, roomId);
        return ResponseEntity.noContent().build();
    }
}
