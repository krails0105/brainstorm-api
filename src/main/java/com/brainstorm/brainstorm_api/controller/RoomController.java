package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.common.ApiResponse;
import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.dto.ShareResponse;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.service.RoomService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Room", description = "브레인스토밍 룸 CRUD")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Room>>> listRooms(@RequestParam(defaultValue = "0") int page) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(roomService.getRooms(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Room>> getRoom(@PathVariable Long id) {
        Room room = roomService.getRoomById(id).orElseThrow(
            () -> new java.util.NoSuchElementException("Not Found Room"));
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Room>> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        UUID userId = (UUID) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        Room saved = roomService.save(roomRequest, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(201, saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Room>> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest roomRequest) {
        UUID userId = (UUID) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        Room save = roomService.update(id, roomRequest, userId);
        return ResponseEntity.ok(ApiResponse.success(save));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        UUID userId = (UUID) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        roomService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<ShareResponse>> addShareToken(@PathVariable Long id) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String shareUrl = roomService.getShareToken(id, userId);
        return ResponseEntity.ok(ApiResponse.success(new ShareResponse(shareUrl)));
    }
}
