package com.brainstorm.brainstorm_api.dto;

import com.brainstorm.brainstorm_api.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomResponse {

    private Long id;
    private String name;
    private String topic;

    public static RoomResponse of(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getTopic());
    }
}
