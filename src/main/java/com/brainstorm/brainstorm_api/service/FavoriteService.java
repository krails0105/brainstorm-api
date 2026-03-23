package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.entity.Favorite;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.FavoriteRepository;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public Page<Favorite> getFavoriteByUserId(UUID userId, Pageable pageable) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Favorite save(UUID userId, Long roomId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Not Found User"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NoSuchElementException("Not Found Room"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRoom(room);
        return favoriteRepository.save(favorite);
    }

    public void delete(UUID userId, Long roomId) {
        Favorite favorite = favoriteRepository.findByUserIdAndRoomId
            (userId, roomId).orElseThrow(() -> new NoSuchElementException("Not Found Favorite"));

        favoriteRepository.delete(favorite);
    }
}
