package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = "";
        String name = "";
        String sub = "";
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            sub = oAuth2User.getAttribute("sub");
        } else if ("kakao".equals(registrationId)) {
            sub = String.valueOf(oAuth2User.getAttribute("id"));
            Map<String, Object> account = oAuth2User.getAttribute("kakao_account");
            email = (String) account.get("email");
            Map<String, Object> properties = oAuth2User.getAttribute("properties");
            name = (String) properties.get("nickname");
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = oAuth2User.getAttribute("response");
            sub = (String) response.get("id");
            email = (String) response.get("email");
            name = (String) response.get("name");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setNickname(name);
            newUser.setProvider(registrationId.toUpperCase());
            newUser.setProviderId(sub);
            userRepository.save(newUser);
        } else {
            user.setProvider(registrationId.toUpperCase());
            user.setProviderId(sub);
            userRepository.save(user);
        }

        return oAuth2User;
    }
}
