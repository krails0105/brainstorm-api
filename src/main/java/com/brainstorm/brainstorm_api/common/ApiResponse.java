package com.brainstorm.brainstorm_api.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

// null인 필드는 JSON에서 제외 (성공 시 error 없음, 에러 시 data 없음)
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data, null);
    }

    public static <T> ApiResponse<T> success(int status, T data) {
        return new ApiResponse<>(status, data, null);
    }

    public static <T> ApiResponse<T> error(int status, String error) {
        return new ApiResponse<>(status, null, error);
    }
}
