package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.common.exception.UnauthorizedAccessException;
import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.RoomRepository;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setNickname("testUser");
        testUser.setPassword("password");
        userRepository.save(testUser);

        otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setNickname("otherUser");
        otherUser.setPassword("password");
        userRepository.save(otherUser);
    }

    private RoomRequest createRoomRequest(String name, String topic) {
        RoomRequest request = new RoomRequest();
        request.setName(name);
        request.setTopic(topic);
        request.setIsPublic(true);
        return request;
    }

    @Test
    void save_shouldCreateRoomAndAddOwnerAsMember() {
        // given
        RoomRequest request = createRoomRequest("Test Room", "Test Topic");

        // when
        Room saved = roomService.save(request, testUser.getId());

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Room");
        assertThat(saved.getTopic()).isEqualTo("Test Topic");
        assertThat(saved.getOwner().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void getRooms_shouldReturnPagedRooms() {
        // given
        roomService.save(createRoomRequest("Room 1", "Topic 1"), testUser.getId());
        roomService.save(createRoomRequest("Room 2", "Topic 2"), testUser.getId());

        // when
        Page<Room> rooms = roomService.getRooms(PageRequest.of(0, 10));

        // then
        assertThat(rooms.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void getRoomById_shouldReturnRoom() {
        // given
        Room saved = roomService.save(createRoomRequest("Find Me", "Topic"), testUser.getId());

        // when
        Optional<Room> found = roomService.getRoomById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Find Me");
    }

    @Test
    void update_shouldChangeNameAndTopic() {
        // given
        Room saved = roomService.save(createRoomRequest("Old Name", "Old Topic"), testUser.getId());

        // when - owner가 수정
        RoomRequest updateRequest = createRoomRequest("New Name", "New Topic");
        Room updated = roomService.update(saved.getId(), updateRequest, testUser.getId());

        // then
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getTopic()).isEqualTo("New Topic");
    }

    @Test
    void update_shouldThrowWhenNotOwner() {
        // given - testUser가 만든 룸
        Room saved = roomService.save(createRoomRequest("Room", "Topic"), testUser.getId());

        // when & then - otherUser가 수정 시도 → 권한 없음
        RoomRequest updateRequest = createRoomRequest("Hacked", "Hacked");
        assertThatThrownBy(() -> roomService.update(saved.getId(), updateRequest, otherUser.getId()))
            .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void delete_shouldRemoveRoom() {
        // given
        Room saved = roomService.save(createRoomRequest("Delete Me", "Topic"), testUser.getId());

        // when - owner가 삭제
        roomService.delete(saved.getId(), testUser.getId());

        // then
        Optional<Room> found = roomService.getRoomById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void delete_shouldThrowWhenNotOwner() {
        // given - testUser가 만든 룸
        Room saved = roomService.save(createRoomRequest("Room", "Topic"), testUser.getId());

        // when & then - otherUser가 삭제 시도 → 권한 없음
        assertThatThrownBy(() -> roomService.delete(saved.getId(), otherUser.getId()))
            .isInstanceOf(UnauthorizedAccessException.class);
    }
}
