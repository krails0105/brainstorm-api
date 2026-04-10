package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.common.exception.RoomFullException;
import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.RoomMemberRepository;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ShareServiceTest {

    @Autowired
    private ShareService shareService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomMemberRepository roomMemberRepository;

    private User owner;
    private User guest;
    private Room room;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setEmail("owner@example.com");
        owner.setNickname("owner");
        owner.setPassword("password");
        userRepository.save(owner);

        guest = new User();
        guest.setEmail("guest@example.com");
        guest.setNickname("guest");
        guest.setPassword("password");
        userRepository.save(guest);

        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setName("Test Room");
        roomRequest.setTopic("Topic");
        roomRequest.setIsPublic(true);
        roomRequest.setTotalUserCount(10);
        room = roomService.save(roomRequest, owner.getId());
    }

    @Test
    void 공유_토큰으로_룸_입장_성공() {
        Room joined = shareService.joinRoomByShareToken(room.getShareToken(), guest.getId());

        assertThat(joined.getId()).isEqualTo(room.getId());
        assertThat(roomMemberRepository.existsByRoomIdAndUserId(room.getId(), guest.getId())).isTrue();
    }

    @Test
    void 이미_멤버인_유저가_다시_입장해도_중복_추가_안됨() {
        shareService.joinRoomByShareToken(room.getShareToken(), guest.getId());
        shareService.joinRoomByShareToken(room.getShareToken(), guest.getId());

        long count = roomMemberRepository.countByRoomId(room.getId());
        assertThat(count).isEqualTo(2); // owner + guest
    }

    @Test
    void 존재하지_않는_토큰으로_입장_실패() {
        assertThatThrownBy(() -> shareService.joinRoomByShareToken("invalid-token", guest.getId()))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 인원이_가득_찬_룸에_입장_시_실패() {
        // given - totalUserCount를 1로 설정 (owner가 이미 1명)
        room.setTotalUserCount(1);

        // when & then - 가득 찬 룸에 입장 시도 → RoomFullException 발생
        assertThatThrownBy(() -> shareService.joinRoomByShareToken(room.getShareToken(), guest.getId()))
            .isInstanceOf(RoomFullException.class);
    }
}
