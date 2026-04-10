package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.dto.ChatMessageResponse;
import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.entity.Room;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

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
        owner.setEmail("owner@example.com");
        owner.setNickname("owner");
        owner.setPassword("password");
        userRepository.save(owner);

        member = new User();
        member.setEmail("member@example.com");
        member.setNickname("member");
        member.setPassword("password");
        userRepository.save(member);

        RoomRequest roomRequest = new RoomRequest();
        roomRequest.setName("Test Room");
        roomRequest.setTopic("Topic");
        roomRequest.setIsPublic(true);
        roomRequest.setTotalUserCount(10);
        room = roomService.save(roomRequest, owner.getId());
    }

    @Test
    void 메시지_저장_성공() {
        // 메시지 저장 후 반환된 DTO 검증
        ChatMessageResponse response = chatService.save(room.getId(), owner.getId(), "안녕하세요");

        assertThat(response.getId()).isNotNull();
        assertThat(response.getContent()).isEqualTo("안녕하세요");
        assertThat(response.getNickname()).isEqualTo("owner");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void 룸의_채팅_히스토리_조회() {
        // 여러 메시지 저장
        chatService.save(room.getId(), owner.getId(), "첫 번째 메시지");
        chatService.save(room.getId(), member.getId(), "두 번째 메시지");
        chatService.save(room.getId(), owner.getId(), "세 번째 메시지");

        // 히스토리 조회
        List<ChatMessageResponse> messages = chatService.getChatMessagesByRoomId(room.getId());

        assertThat(messages).hasSize(3);
        // 시간순 정렬 확인
        assertThat(messages.get(0).getContent()).isEqualTo("첫 번째 메시지");
        assertThat(messages.get(1).getContent()).isEqualTo("두 번째 메시지");
        assertThat(messages.get(2).getContent()).isEqualTo("세 번째 메시지");
    }

    @Test
    void 다른_룸의_메시지는_조회되지_않음() {
        // 다른 룸 생성
        RoomRequest anotherRoomRequest = new RoomRequest();
        anotherRoomRequest.setName("Another Room");
        anotherRoomRequest.setTopic("Another Topic");
        anotherRoomRequest.setIsPublic(true);
        anotherRoomRequest.setTotalUserCount(10);
        Room anotherRoom = roomService.save(anotherRoomRequest, owner.getId());

        // 각 룸에 메시지 저장
        chatService.save(room.getId(), owner.getId(), "룸1 메시지");
        chatService.save(anotherRoom.getId(), owner.getId(), "룸2 메시지");

        // 룸1 히스토리 조회 시 룸2 메시지는 안 나옴
        List<ChatMessageResponse> messages = chatService.getChatMessagesByRoomId(room.getId());

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo("룸1 메시지");
    }

    @Test
    void 메시지_없는_룸_조회_시_빈_리스트() {
        List<ChatMessageResponse> messages = chatService.getChatMessagesByRoomId(room.getId());

        assertThat(messages).isEmpty();
    }

    @Test
    void 존재하지_않는_룸에_메시지_저장_시_실패() {
        // 존재하지 않는 roomId로 메시지 저장 시도
        assertThatThrownBy(() -> chatService.save(9999L, owner.getId(), "메시지"))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void 존재하지_않는_유저가_메시지_저장_시_실패() {
        // 존재하지 않는 userId로 메시지 저장 시도
        UUID fakeUserId = UUID.randomUUID();
        assertThatThrownBy(() -> chatService.save(room.getId(), fakeUserId, "메시지"))
            .isInstanceOf(NoSuchElementException.class);
    }
}
