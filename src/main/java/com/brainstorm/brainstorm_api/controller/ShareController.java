package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.dto.RoomResponse;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.service.ShareService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Share", description = "공유 링크")
@RestController
@RequestMapping("/api/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @GetMapping("/{token}")
    public ResponseEntity<RoomResponse> joinRoomByToken(@PathVariable String token) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Room room = shareService.joinRoomByShareToken(token, userId);
        return ResponseEntity.ok(RoomResponse.of(room));
    }
}
