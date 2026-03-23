package com.brainstorm.brainstorm_api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brainstorm.brainstorm_api.common.exception.DuplicateEmailException;
import com.brainstorm.brainstorm_api.common.exception.InvalidCredentialsException;
import com.brainstorm.brainstorm_api.dto.LoginRequest;
import com.brainstorm.brainstorm_api.dto.LoginResponse;
import com.brainstorm.brainstorm_api.dto.SignupRequest;
import com.brainstorm.brainstorm_api.dto.SignupResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    private SignupRequest createSignupRequest(String email, String password, String nickname) {
        SignupRequest request = new SignupRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setNickname(nickname);
        return request;
    }

    private LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    @Test
    void 회원가입_성공() {
        SignupResponse response = authService.signUp(
            createSignupRequest("test@example.com", "password123", "테스터"));

        assertThat(response.getUserId()).isNotNull();
    }

    @Test
    void 이메일_중복_회원가입_실패() {
        authService.signUp(createSignupRequest("dup@example.com", "password123", "테스터"));

        assertThatThrownBy(() -> authService.signUp(
            createSignupRequest("dup@example.com", "password456", "테스터2")))
            .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void 로그인_성공() {
        authService.signUp(createSignupRequest("login@example.com", "password123", "로그인유저"));

        LoginResponse response = authService.signIn(
            createLoginRequest("login@example.com", "password123"));

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getUser().getNickname()).isEqualTo("로그인유저");
    }

    @Test
    void 잘못된_비밀번호_로그인_실패() {
        authService.signUp(createSignupRequest("wrong@example.com", "password123", "유저"));

        assertThatThrownBy(() -> authService.signIn(
            createLoginRequest("wrong@example.com", "wrongpassword")))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void 존재하지_않는_이메일_로그인_실패() {
        assertThatThrownBy(() -> authService.signIn(
            createLoginRequest("noone@example.com", "password123")))
            .isInstanceOf(InvalidCredentialsException.class);
    }
}
