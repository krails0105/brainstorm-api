package com.brainstorm.brainstorm_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeywordRequest {

    @NotBlank
    private String content;

}
