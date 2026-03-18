package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.dto.RoomMemberRequest;
import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberService roomMemberService;

    public Page<Room> getRooms(Pageable pageable) {
        return roomRepository.findAll(pageable);
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    @Transactional
    public Room save(RoomRequest roomRequest) {
        Room newRoom = new Room();
        newRoom.setName(roomRequest.getName());
        newRoom.setOwner(roomRequest.getOwner());
        newRoom.setTopic(roomRequest.getTopic());
        newRoom.setTotalUserCount(10);
        newRoom.setIsPublic(roomRequest.getIsPublic());
        Room savedRoom = roomRepository.save(newRoom);

        RoomMemberRequest roomMemberRequest = new RoomMemberRequest();
        roomMemberRequest.setRoomId(savedRoom.getId());
        roomMemberRequest.setUserId(savedRoom.getOwner().getId());
        roomMemberService.save(roomMemberRequest);
        return savedRoom;
    }

    public Room update(Long id, Room room) {
        Room updateRoom = roomRepository.findById(id).orElseThrow();
        updateRoom.setName(room.getName());
        updateRoom.setTopic(room.getTopic());
        return roomRepository.save(updateRoom);
    }

    public void delete(Long id) {
        roomRepository.deleteById(id);
    }
}
