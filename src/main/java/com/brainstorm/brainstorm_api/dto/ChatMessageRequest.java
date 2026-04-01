package com.brainstorm.brainstorm_api.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    private UUID userId;

    private String content;

}
