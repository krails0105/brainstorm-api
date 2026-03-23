package com.brainstorm.brainstorm_api.dto;

import com.brainstorm.brainstorm_api.entity.User;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;

    private UserInfo user;

    @Setter
    @Getter
    @NoArgsConstructor
    public static class UserInfo {

        private UUID id;

        private String nickname;

        public static UserInfo ofUser(User user) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setNickname(user.getNickname());

            return userInfo;
        }
    }
}
