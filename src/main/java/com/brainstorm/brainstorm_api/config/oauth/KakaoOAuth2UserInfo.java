package com.brainstorm.brainstorm_api.config.oauth;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo{

    private final Map<String, Object> attributes;

    KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getEmail() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        return (String) account.get("email");
    }

    @Override
    public String getNickname() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return (String) properties.get("nickname");
    }

    @Override
    public String getProviderId() {
        return String.valueOf(this.attributes.get("id"));
    }
}
