package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        testUser = new User();
        testUser.setName("testUser");
        testUser.setPassword("password");
        userRepository.save(testUser);
    }

    @Test
    void save_shouldCreateRoomAndAddOwnerAsMember() {
        // given - 룸 생성 요청 준비
        RoomRequest request = new RoomRequest();
        request.setOwner(testUser);
        request.setName("Test Room");
        request.setTopic("Test Topic");
        request.setIsPublic(true);

        // when - 룸 생성
        Room saved = roomService.save(request);

        // then - 룸이 저장되었는지 확인
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Room");
        assertThat(saved.getTopic()).isEqualTo("Test Topic");
        assertThat(saved.getOwner().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void getRooms_shouldReturnPagedRooms() {
        // given - 룸 2개 생성
        RoomRequest request1 = new RoomRequest();
        request1.setOwner(testUser);
        request1.setName("Room 1");
        request1.setTopic("Topic 1");
        request1.setIsPublic(true);
        roomService.save(request1);

        RoomRequest request2 = new RoomRequest();
        request2.setOwner(testUser);
        request2.setName("Room 2");
        request2.setTopic("Topic 2");
        request2.setIsPublic(true);
        roomService.save(request2);

        // when - 페이징 조회
        Page<Room> rooms = roomService.getRooms(PageRequest.of(0, 10));

        // then - 2개 이상 존재하는지 확인
        assertThat(rooms.getTotalElements()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void getRoomById_shouldReturnRoom() {
        // given - 룸 생성
        RoomRequest request = new RoomRequest();
        request.setOwner(testUser);
        request.setName("Find Me");
        request.setTopic("Topic");
        request.setIsPublic(true);
        Room saved = roomService.save(request);

        // when - ID로 조회
        Optional<Room> found = roomService.getRoomById(saved.getId());

        // then - 조회 결과 확인
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Find Me");
    }

    @Test
    void update_shouldChangeNameAndTopic() {
        // given - 룸 생성
        RoomRequest request = new RoomRequest();
        request.setOwner(testUser);
        request.setName("Old Name");
        request.setTopic("Old Topic");
        request.setIsPublic(true);
        Room saved = roomService.save(request);

        // when - 이름과 주제 변경
        Room updateData = new Room();
        updateData.setName("New Name");
        updateData.setTopic("New Topic");
        Room updated = roomService.update(saved.getId(), updateData);

        // then - 변경 확인
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getTopic()).isEqualTo("New Topic");
    }

    @Test
    void delete_shouldRemoveRoom() {
        // given - 룸 생성
        RoomRequest request = new RoomRequest();
        request.setOwner(testUser);
        request.setName("Delete Me");
        request.setTopic("Topic");
        request.setIsPublic(true);
        Room saved = roomService.save(request);

        // when - 삭제
        roomService.delete(saved.getId());

        // then - 조회 불가 확인
        Optional<Room> found = roomService.getRoomById(saved.getId());
        assertThat(found).isEmpty();
    }
}
