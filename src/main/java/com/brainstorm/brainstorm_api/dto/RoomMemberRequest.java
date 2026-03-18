package com.brainstorm.brainstorm_api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoomMemberRequest {

    private Long userId;
    private Long roomId;
}
