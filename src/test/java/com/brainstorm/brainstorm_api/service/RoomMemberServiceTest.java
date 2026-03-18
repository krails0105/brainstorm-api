package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.common.exception.RoomFullException;
import com.brainstorm.brainstorm_api.dto.RoomMemberRequest;
import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.RoomMember;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RoomMemberServiceTest {

    @Autowired
    private RoomMemberService roomMemberService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User member;
    private Room room;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 2명 생성
        owner = new User();
        owner.setName("owner");
        owner.setPassword("password");
        userRepository.save(owner);

        member = new User();
        member.setName("member");
        member.setPassword("password");
        userRepository.save(member);

        // 룸 생성 (owner가 자동으로 멤버에 추가됨)
        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setOwner(owner);
        roomRequest.setName("Test Room");
        roomRequest.setTopic("Topic");
        roomRequest.setIsPublic(true);
        room = roomService.save(roomRequest);
    }

    @Test
    void save_shouldAddMemberToRoom() {
        // given - totalUserCount를 늘려서 멤버 추가 가능하게 설정
        room.setTotalUserCount(2);

        RoomMemberRequest request = new RoomMemberRequest();
        request.setRoomId(room.getId());
        request.setUserId(member.getId());

        // when - 멤버 추가
        RoomMember saved = roomMemberService.save(request);

        // then - 멤버 추가 확인
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRoom().getId()).isEqualTo(room.getId());
        assertThat(saved.getUser().getId()).isEqualTo(member.getId());
    }

    @Test
    void getRoomMembers_shouldReturnMembers() {
        // given - owner는 이미 멤버로 추가됨

        // when - 멤버 목록 조회
        List<RoomMember> members = roomMemberService.getRoomMembers(room.getId());

        // then - owner 1명 존재
        assertThat(members).hasSize(1);
    }

    @Test
    void getRoomMembersCount_shouldReturnCount() {
        // given - owner는 이미 멤버로 추가됨

        // when - 멤버 수 조회
        long count = roomMemberService.getRoomMembersCount(room.getId());

        // then - 1명
        assertThat(count).isEqualTo(1);
    }

    @Test
    void save_shouldThrowRoomFullExceptionWhenRoomIsFull() {
        // given - totalUserCount가 1이므로 이미 가득 참 (owner가 있으니)
        RoomMemberRequest request = new RoomMemberRequest();
        request.setRoomId(room.getId());
        request.setUserId(member.getId());

        // when & then - 인원 초과 시 RoomFullException 발생
        assertThatThrownBy(() -> roomMemberService.save(request))
            .isInstanceOf(RoomFullException.class)
            .hasMessage("Max Member Exceed!");
    }

    @Test
    void delete_shouldRemoveMember() {
        // given - 먼저 totalUserCount를 늘려서 멤버 추가 가능하게 하고, 멤버 추가
        room.setTotalUserCount(2);

        RoomMemberRequest addRequest = new RoomMemberRequest();
        addRequest.setRoomId(room.getId());
        addRequest.setUserId(member.getId());
        roomMemberService.save(addRequest);
        assertThat(roomMemberService.getRoomMembersCount(room.getId())).isEqualTo(2);

        // when - 멤버 삭제
        RoomMemberRequest deleteRequest = new RoomMemberRequest();
        deleteRequest.setRoomId(room.getId());
        deleteRequest.setUserId(member.getId());
        roomMemberService.delete(deleteRequest);

        // then - 멤버 수 감소 확인
        assertThat(roomMemberService.getRoomMembersCount(room.getId())).isEqualTo(1);
    }
}
