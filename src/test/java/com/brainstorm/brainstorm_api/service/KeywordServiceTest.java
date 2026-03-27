package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.common.exception.KeywordFullException;
import com.brainstorm.brainstorm_api.common.exception.UnauthorizedAccessException;
import com.brainstorm.brainstorm_api.dto.KeywordRequest;
import com.brainstorm.brainstorm_api.dto.KeywordResponse;
import com.brainstorm.brainstorm_api.dto.RoomRequest;
import com.brainstorm.brainstorm_api.entity.Keyword;
import com.brainstorm.brainstorm_api.entity.Room;
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
class KeywordServiceTest {

    @Autowired
    private KeywordService keywordService;

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

    private KeywordRequest createKeywordRequest(String content) {
        KeywordRequest request = new KeywordRequest();
        request.setContent(content);
        return request;
    }

    @Test
    void 키워드_생성_성공() {
        Keyword saved = keywordService.save(room.getId(), member.getId(), createKeywordRequest("자동화"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getContent()).isEqualTo("자동화");
        assertThat(saved.getUser().getId()).isEqualTo(member.getId());
    }

    @Test
    void 룸의_키워드_목록_조회() {
        keywordService.save(room.getId(), owner.getId(), createKeywordRequest("AI"));
        keywordService.save(room.getId(), member.getId(), createKeywordRequest("자동화"));

        List<KeywordResponse> keywords = keywordService.getKeywordsByRoomId(room.getId(), owner.getId());

        assertThat(keywords).hasSize(2);
    }

    @Test
    void 키워드_10개_초과_시_실패() {
        for (int i = 0; i < 10; i++) {
            keywordService.save(room.getId(), member.getId(), createKeywordRequest("키워드" + i));
        }

        assertThatThrownBy(() -> keywordService.save(room.getId(), member.getId(), createKeywordRequest("초과")))
            .isInstanceOf(KeywordFullException.class);
    }

    @Test
    void 본인_키워드_삭제_성공() {
        Keyword saved = keywordService.save(room.getId(), member.getId(), createKeywordRequest("삭제대상"));

        keywordService.delete(saved.getId(), member.getId());

        List<KeywordResponse> keywords = keywordService.getKeywordsByRoomId(room.getId(), member.getId());
        assertThat(keywords).isEmpty();
    }

    @Test
    void Room_owner가_다른_유저_키워드_삭제_성공() {
        Keyword saved = keywordService.save(room.getId(), member.getId(), createKeywordRequest("삭제대상"));

        // owner가 member의 키워드 삭제
        keywordService.delete(saved.getId(), owner.getId());

        List<KeywordResponse> keywords = keywordService.getKeywordsByRoomId(room.getId(), owner.getId());
        assertThat(keywords).isEmpty();
    }

    @Test
    void 권한_없는_유저가_삭제_시_실패() {
        Keyword saved = keywordService.save(room.getId(), owner.getId(), createKeywordRequest("키워드"));

        // member가 owner의 키워드 삭제 시도
        assertThatThrownBy(() -> keywordService.delete(saved.getId(), member.getId()))
            .isInstanceOf(UnauthorizedAccessException.class);
    }
}
