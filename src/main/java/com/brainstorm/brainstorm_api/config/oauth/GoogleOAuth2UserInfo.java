package com.brainstorm.brainstorm_api.config.oauth;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo{

    private final Map<String, Object> attributes;

    GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getEmail() {
        return (String) this.attributes.get("email");
    }

    @Override
    public String getNickname() {
        return (String) this.attributes.get("name");
    }

    @Override
    public String getProviderId() {
        return (String) this.attributes.get("sub");
    }
}
