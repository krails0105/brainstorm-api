package com.brainstorm.brainstorm_api.dto;

import com.brainstorm.brainstorm_api.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomRequest {

    private User owner;

    private String name;

    private String topic;

    private Boolean isPublic;
}
