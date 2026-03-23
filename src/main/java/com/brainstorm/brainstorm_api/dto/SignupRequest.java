package com.brainstorm.brainstorm_api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class SignupRequest {

    private String email;

    private String password;

    private String nickname;
}
