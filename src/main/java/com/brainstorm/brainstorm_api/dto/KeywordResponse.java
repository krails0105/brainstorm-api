package com.brainstorm.brainstorm_api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KeywordResponse {

    private Long id;

    private String content;

    private String nickname;

    private long likeCount;

    private boolean liked;

}
