package com.brainstorm.brainstorm_api.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void 토큰_생성_및_검증() {
        UUID userId = UUID.randomUUID();

        String token = jwtProvider.createToken(userId);

        assertThat(jwtProvider.validateToken(token)).isTrue();
        assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    void 잘못된_토큰은_검증_실패() {
        assertThat(jwtProvider.validateToken("invalid.token.here")).isFalse();
    }
}
