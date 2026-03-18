package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.dto.FavoriteRequest;
import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.entity.Favorite;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.NoSuchElementException;
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

    private User testUser;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        testUser = new User();
        testUser.setName("testUser");
        testUser.setPassword("password");
        userRepository.save(testUser);

        // 테스트용 룸 생성
        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setOwner(testUser);
        roomRequest.setName("Test Room");
        roomRequest.setTopic("Topic");
        roomRequest.setIsPublic(true);
        testRoom = roomService.save(roomRequest);
    }

    @Test
    void save_shouldCreateFavorite() {
        // given - 즐겨찾기 요청
        FavoriteRequest request = new FavoriteRequest();
        request.setUserId(testUser.getId());
        request.setRoomId(testRoom.getId());

        // when - 즐겨찾기 추가
        Favorite saved = favoriteService.save(request);

        // then - 즐겨찾기 저장 확인
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(saved.getRoom().getId()).isEqualTo(testRoom.getId());
    }

    @Test
    void getFavoriteByUserId_shouldReturnFavorites() {
        // given - 즐겨찾기 추가
        FavoriteRequest request = new FavoriteRequest();
        request.setUserId(testUser.getId());
        request.setRoomId(testRoom.getId());
        favoriteService.save(request);

        // when - 유저의 즐겨찾기 목록 조회
        Page<Favorite> favorites = favoriteService.getFavoriteByUserId(
            testUser.getId(), PageRequest.of(0, 10));

        // then - 1개 존재
        assertThat(favorites.getTotalElements()).isEqualTo(1);
    }

    @Test
    void delete_shouldRemoveFavorite() {
        // given - 즐겨찾기 추가
        FavoriteRequest addRequest = new FavoriteRequest();
        addRequest.setUserId(testUser.getId());
        addRequest.setRoomId(testRoom.getId());
        favoriteService.save(addRequest);

        // when - 즐겨찾기 삭제
        FavoriteRequest deleteRequest = new FavoriteRequest();
        deleteRequest.setUserId(testUser.getId());
        deleteRequest.setRoomId(testRoom.getId());
        favoriteService.delete(deleteRequest);

        // then - 즐겨찾기 0개
        Page<Favorite> favorites = favoriteService.getFavoriteByUserId(
            testUser.getId(), PageRequest.of(0, 10));
        assertThat(favorites.getTotalElements()).isEqualTo(0);
    }

    @Test
    void delete_shouldThrowWhenFavoriteNotFound() {
        // given - 즐겨찾기가 없는 상태
        FavoriteRequest request = new FavoriteRequest();
        request.setUserId(testUser.getId());
        request.setRoomId(testRoom.getId());

        // when & then - 존재하지 않는 즐겨찾기 삭제 시 예외 발생
        assertThatThrownBy(() -> favoriteService.delete(request))
            .isInstanceOf(NoSuchElementException.class);
    }
}
