package com.brainstorm.brainstorm_api.config.oauth;

import java.util.Map;

public interface OAuth2UserInfo {
    String getEmail();
    String getNickname();
    String getProviderId();

    static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            case "naver" -> new NaverOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported Provider: " + registrationId);
        };
    }
}
