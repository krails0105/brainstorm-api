package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.common.RoomRole;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.repository.RoomMemberRepository;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomMemberService roomMemberService;

    public Room joinRoomByShareToken(String token, UUID userId) {
        Room room = roomRepository.findByShareToken(token).orElseThrow(() -> new NoSuchElementException("Not Found Room"));
        boolean exist = roomMemberRepository.existsByRoomIdAndUserId(room.getId(), userId);
        if (!exist) roomMemberService.save(room.getId(), userId, RoomRole.MEMBER);

        return room;
    }
}
