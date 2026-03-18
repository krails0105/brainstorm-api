package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.dto.FavoriteRequest;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@AllArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/{id}")
    public Page<Favorite> listFavorite(@PathVariable Long id,
        @RequestParam(defaultValue = "0") int page) {

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        return favoriteService.getFavoriteByUserId(id, pageable);
    }

    @PostMapping
    public ResponseEntity<Favorite> createFavorite(@RequestBody FavoriteRequest favoriteRequest) {
        Favorite save = favoriteService.save(favoriteRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(save);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteFavorite(@RequestBody FavoriteRequest favoriteRequest) {
        favoriteService.delete(favoriteRequest);
        return ResponseEntity.noContent().build();
    }
}
