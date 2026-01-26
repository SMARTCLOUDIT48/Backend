package com.scit48.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private ApiStatus status;
    private T data;
    private String message;

    /*
     * =========================
     * 성공
     * =========================
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                ApiStatus.SUCCESS,
                data,
                "요청이 성공적으로 처리되었습니다.");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(
                ApiStatus.SUCCESS,
                data,
                message);
    }

    /*
     * =========================
     * 실패
     * =========================
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(
                ApiStatus.ERROR,
                null,
                message);
    }
}
