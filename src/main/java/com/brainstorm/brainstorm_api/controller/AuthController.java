package com.brainstorm.brainstorm_api.controller;

import com.brainstorm.brainstorm_api.dto.LoginRequest;
import com.brainstorm.brainstorm_api.dto.LoginResponse;
import com.brainstorm.brainstorm_api.dto.LoginResponse.UserInfo;
import com.brainstorm.brainstorm_api.dto.SignupRequest;
import com.brainstorm.brainstorm_api.dto.SignupResponse;
import com.brainstorm.brainstorm_api.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "회원가입/로그인")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signUp(@RequestBody SignupRequest signupRequest) {
        SignupResponse signupResponse = authService.signUp(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.logIn(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/status")
    public ResponseEntity<LoginResponse.UserInfo> status() {
        UserInfo userInfo = authService.tokenValidation();
        return ResponseEntity.ok(userInfo);
    }
}
