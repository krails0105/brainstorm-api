package com.brainstorm.brainstorm_api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomRequest {

    private String name;

    private String topic;

    private Boolean isPublic;

    private int totalUserCount;
}
