package com.brainstorm.brainstorm_api.service;

import com.brainstorm.brainstorm_api.config.oauth.OAuth2UserInfo;
import com.brainstorm.brainstorm_api.entity.User;
import com.brainstorm.brainstorm_api.repository.UserRepository;
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

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(registrationId, oAuth2User.getAttributes());

        String email = userInfo.getEmail();
        String name = userInfo.getNickname();
        String sub = userInfo.getProviderId();

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
