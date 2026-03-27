package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.common.RoomRole;
import com.brainstorm.brainstorm_api.common.exception.UnauthorizedAccessException;
import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberService roomMemberService;
    private final UserRepository userRepository;

    @Value("${app.frontend-url}")
    private String appUrl;

    public Page<Room> getRooms(Pageable pageable) {
        return roomRepository.findAll(pageable);
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    @Transactional
    public Room save(RoomRequest roomRequest, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Not Found User"));

        Room newRoom = new Room();
        newRoom.setOwner(user);
        newRoom.setName(roomRequest.getName());
        newRoom.setTopic(roomRequest.getTopic());
        int totalUserCount = roomRequest.getTotalUserCount();
        if (totalUserCount < 1 || totalUserCount > 12) {
            throw new IllegalStateException("Total user count have to be between 1 and 12");
        }
        newRoom.setTotalUserCount(totalUserCount);
        newRoom.setIsPublic(roomRequest.getIsPublic());
        Room savedRoom = roomRepository.save(newRoom);

        roomMemberService.save(savedRoom.getId(), savedRoom.getOwner().getId(), RoomRole.OWNER);
        return savedRoom;
    }

    public Room update(Long id, RoomRequest roomRequest, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Not Found User"));
        Room updateRoom = roomRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not Found Room"));
        UUID ownerId = updateRoom.getOwner().getId();
        if (!user.getId().equals(ownerId)) {
            throw new UnauthorizedAccessException("Unauthorized User");
        }

        updateRoom.setName(roomRequest.getName());
        updateRoom.setTopic(roomRequest.getTopic());
        updateRoom.setIsPublic(roomRequest.getIsPublic());
        int totalUserCount = roomRequest.getTotalUserCount();
        if (totalUserCount < 1 || totalUserCount > 12) {
            throw new IllegalStateException("Total user count have to be between 1 and 12");
        }
        updateRoom.setTotalUserCount(totalUserCount);
        return roomRepository.save(updateRoom);
    }

    public void delete(Long id, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Not Found User"));
        Room room = roomRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Not Found Room"));
        if (!user.getId().equals(room.getOwner().getId())) {
            throw new UnauthorizedAccessException("Unauthorized User");
        }

        roomRepository.deleteById(id);
    }

    public String getShareToken(Long roomId, UUID userId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NoSuchElementException("Not Found Room"));

        if (!room.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Only access for room owner");
        }

        String shareToken = UUID.randomUUID().toString();
        room.setShareToken(shareToken);
        roomRepository.save(room);
        return appUrl + "/join/" + shareToken;
    }
}
