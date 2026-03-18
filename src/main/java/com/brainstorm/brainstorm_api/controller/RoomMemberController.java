package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.dto.RoomMemberRequest;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@AllArgsConstructor
public class RoomMemberController {

    private final RoomMemberService roomMemberService;

    @GetMapping("/{id}")
    public List<RoomMember> listRoomMember(@PathVariable Long id) {
        return roomMemberService.getRoomMembers(id);
    }

    @PostMapping
    public ResponseEntity<RoomMember> addRoomMember(@RequestBody RoomMemberRequest roomMemberRequest) {
        RoomMember save = roomMemberService.save(roomMemberRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(save);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteRoomMember(@RequestBody RoomMemberRequest roomMemberRequest) {
        roomMemberService.delete(roomMemberRequest);
        return ResponseEntity.noContent().build();
    }
}
