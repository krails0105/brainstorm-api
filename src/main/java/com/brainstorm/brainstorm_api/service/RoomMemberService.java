package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.common.exception.RoomFullException;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.RoomMember;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.RoomMemberRepository;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomMemberService {

    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public List<RoomMember> getRoomMembers(Long roomId) {
        return roomMemberRepository.findByRoomId(roomId);
    }

    public long getRoomMembersCount(Long roomId) {
        return roomMemberRepository.countByRoomId(roomId);
    }

    public RoomMember save(Long roomId, UUID userId) {
        Room room = roomRepository.findById(roomId).orElseThrow();
        long roomMembersCount = getRoomMembersCount(room.getId());
        if (roomMembersCount >= room.getTotalUserCount()) {
            throw new RoomFullException("Max Member Exceed!");
        }

        User user = userRepository.findById(userId).orElseThrow();
        RoomMember roomMember = new RoomMember();
        roomMember.setRoom(room);
        roomMember.setUser(user);

        return roomMemberRepository.save(roomMember);
    }

    @Transactional
    public void delete(Long roomId, UUID userId) {
        roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
    }
}
