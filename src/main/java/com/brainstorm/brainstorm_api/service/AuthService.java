package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.common.exception.DuplicateEmailException;
import com.brainstorm.brainstorm_api.common.exception.InvalidCredentialsException;
import com.brainstorm.brainstorm_api.config.jwt.JwtProvider;
import com.brainstorm.brainstorm_api.dto.LoginRequest;
import com.brainstorm.brainstorm_api.dto.LoginResponse;
import com.brainstorm.brainstorm_api.dto.LoginResponse.UserInfo;
import com.brainstorm.brainstorm_api.dto.SignupRequest;
import com.brainstorm.brainstorm_api.dto.SignupResponse;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signUp(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        String password = signupRequest.getPassword();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Duplicated Email");
        }

        String encode = passwordEncoder.encode(password);
        signupRequest.setPassword(encode);
        User user = User.ofSignupRequest(signupRequest);
        userRepository.save(user);
        UUID id = user.getId();

        return new SignupResponse(id);
    }

    public LoginResponse logIn(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException("Not Found Email"));
        String password = loginRequest.getPassword();
        String userPassword = user.getPassword();
        if (user.getPassword() == null) {
            throw new InvalidCredentialsException("소셜 로그인으로 가입된 계정입니다.");
        }

        if (!passwordEncoder.matches(password, userPassword)) {
            throw new InvalidCredentialsException("Not Matched Password");
        }

        String token = jwtProvider.createToken(user.getId());
        UserInfo userInfo = UserInfo.ofUser(user);
        return new LoginResponse(token, userInfo);
    }

    public UserInfo tokenValidation() {
        UUID id = (UUID) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();

        User user = userRepository.findById(id).orElseThrow(() -> new InvalidCredentialsException("Invalid token"));
        return UserInfo.ofUser(user);
    }
}