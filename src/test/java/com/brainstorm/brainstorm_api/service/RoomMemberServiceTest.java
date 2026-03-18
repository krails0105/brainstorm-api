package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.common.exception.RoomFullException;
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
        owner = new User();
        owner.setName("owner");
        owner.setPassword("password");
        userRepository.save(owner);

        member = new User();
        member.setName("member");
        member.setPassword("password");
        userRepository.save(member);

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
        room.setTotalUserCount(11);

        // when - 멤버 추가
        RoomMember saved = roomMemberService.save(room.getId(), member.getId());

        // then - 멤버 추가 확인
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRoom().getId()).isEqualTo(room.getId());
        assertThat(saved.getUser().getId()).isEqualTo(member.getId());
    }

    @Test
    void getRoomMembers_shouldReturnMembers() {
        // when - 멤버 목록 조회
        List<RoomMember> members = roomMemberService.getRoomMembers(room.getId());

        // then - owner 1명 존재
        assertThat(members).hasSize(1);
    }

    @Test
    void getRoomMembersCount_shouldReturnCount() {
        // when - 멤버 수 조회
        long count = roomMemberService.getRoomMembersCount(room.getId());

        // then - 1명
        assertThat(count).isEqualTo(1);
    }

    @Test
    void save_shouldThrowRoomFullExceptionWhenRoomIsFull() {
        // given - totalUserCount를 1로 설정해서 이미 가득 찬 상태로 만듦
        room.setTotalUserCount(1);

        // when & then - 인원 초과 시 RoomFullException 발생
        assertThatThrownBy(() -> roomMemberService.save(room.getId(), member.getId()))
            .isInstanceOf(RoomFullException.class)
            .hasMessage("Max Member Exceed!");
    }

    @Test
    void delete_shouldRemoveMember() {
        // given - 멤버 추가
        room.setTotalUserCount(11);
        roomMemberService.save(room.getId(), member.getId());
        assertThat(roomMemberService.getRoomMembersCount(room.getId())).isEqualTo(2);

        // when - 멤버 삭제
        roomMemberService.delete(room.getId(), member.getId());

        // then - 멤버 수 감소 확인
        assertThat(roomMemberService.getRoomMembersCount(room.getId())).isEqualTo(1);
    }
}
