package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.entity.RoomMember;
import com.brainstorm.brainstorm_api.service.RoomMemberService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms/{roomId}/members")
@AllArgsConstructor
public class RoomMemberController {

    private final RoomMemberService roomMemberService;

    @GetMapping
    public List<RoomMember> listRoomMember(@PathVariable Long roomId) {
        return roomMemberService.getRoomMembers(roomId);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<RoomMember> addRoomMember(@PathVariable Long roomId,
        @PathVariable Long userId) {
        RoomMember save = roomMemberService.save(roomId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(save);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteRoomMember(@PathVariable Long roomId,
        @PathVariable Long userId) {
        roomMemberService.delete(roomId, userId);
        return ResponseEntity.noContent().build();
    }
}
