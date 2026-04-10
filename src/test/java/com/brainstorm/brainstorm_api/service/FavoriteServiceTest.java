package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.entity.Favorite;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FavoriteServiceTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setNickname("testUser");
        testUser.setPassword("password");
        userRepository.save(testUser);

        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setName("Test Room");
        roomRequest.setTopic("Topic");
        roomRequest.setIsPublic(true);
        roomRequest.setTotalUserCount(10);
        testRoom = roomService.save(roomRequest, testUser.getId());
    }

    @Test
    void save_shouldCreateFavorite() {
        // when - 즐겨찾기 추가
        Favorite saved = favoriteService.save(testUser.getId(), testRoom.getId());

        // then - 즐겨찾기 저장 확인
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getRoom().getId()).isEqualTo(testRoom.getId());
    }

    @Test
    void getFavoriteByUserId_shouldReturnFavorites() {
        // given - 즐겨찾기 추가
        favoriteService.save(testUser.getId(), testRoom.getId());

        // when - 유저의 즐겨찾기 목록 조회
        Page<Favorite> favorites = favoriteService.getFavoriteByUserId(
            testUser.getId(), PageRequest.of(0, 10));

        // then - 1개 존재
        assertThat(favorites.getTotalElements()).isEqualTo(1);
    }

    @Test
    void delete_shouldRemoveFavorite() {
        // given - 즐겨찾기 추가
        favoriteService.save(testUser.getId(), testRoom.getId());

        // when - 즐겨찾기 삭제
        favoriteService.delete(testUser.getId(), testRoom.getId());

        // then - 즐겨찾기 0개
        Page<Favorite> favorites = favoriteService.getFavoriteByUserId(
            testUser.getId(), PageRequest.of(0, 10));
        assertThat(favorites.getTotalElements()).isEqualTo(0);
    }

    @Test
    void delete_shouldThrowWhenFavoriteNotFound() {
        // when & then - 존재하지 않는 즐겨찾기 삭제 시 예외 발생
        assertThatThrownBy(() -> favoriteService.delete(testUser.getId(), testRoom.getId()))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void save_shouldThrowWhenDuplicateFavorite() {
        // given - 즐겨찾기 추가
        favoriteService.save(testUser.getId(), testRoom.getId());
        // DB에 반영하여 unique 제약조건이 작동하도록 함
        entityManager.flush();

        // when & then - 같은 유저가 같은 룸 즐겨찾기 중복 추가 시 예외 발생
        assertThatThrownBy(() -> {
            favoriteService.save(testUser.getId(), testRoom.getId());
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void save_shouldThrowWhenUserNotFound() {
        // 존재하지 않는 유저로 즐겨찾기 추가 시 예외 발생
        UUID fakeUserId = UUID.randomUUID();
        assertThatThrownBy(() -> favoriteService.save(fakeUserId, testRoom.getId()))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void save_shouldThrowWhenRoomNotFound() {
        // 존재하지 않는 룸으로 즐겨찾기 추가 시 예외 발생
        assertThatThrownBy(() -> favoriteService.save(testUser.getId(), 9999L))
            .isInstanceOf(NoSuchElementException.class);
    }
}
