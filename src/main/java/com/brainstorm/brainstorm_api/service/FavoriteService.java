package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.dto.FavoriteRequest;
import com.brainstorm.brainstorm_api.entity.Favorite;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.FavoriteRepository;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public Page<Favorite> getFavoriteByUserId(Long userId, Pageable pageable) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Favorite save(FavoriteRequest favoriteRequest) {
        Long userId = favoriteRequest.getUserId();
        Long roomId = favoriteRequest.getRoomId();

        User user = userRepository.findById(userId).orElseThrow();
        Room room = roomRepository.findById(roomId).orElseThrow();

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setRoom(room);
        return favoriteRepository.save(favorite);
    }

    public void delete(FavoriteRequest favoriteRequest) {
        Long userId = favoriteRequest.getUserId();
        Long roomId = favoriteRequest.getRoomId();

        Optional<Favorite> favorite = favoriteRepository.findByUserIdAndRoomId(userId, roomId);
        if (favorite.isEmpty()) {
            throw new NoSuchElementException("Not Found Room");
        }
        favoriteRepository.delete(favorite.get());
    }
}
