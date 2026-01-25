package com.scit48.common.exception;

import com.scit48.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        /*
         * =========================
         * 400 - 잘못된 요청
         * =========================
         */
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException e) {
                return ResponseEntity
                                .badRequest()
                                .body(ApiResponse.error(e.getMessage()));
        }

        /*
         * =========================
         * 401 - 인증 실패
         * =========================
         */
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ApiResponse<?>> handleUnauthorized(UnauthorizedException e) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(e.getMessage()));
        }

        /*
         * =========================
         * 400 - Validation 실패
         * =========================
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {

                String message = e.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                                .findFirst()
                                .orElse("Validation Error");

                return ResponseEntity
                                .badRequest()
                                .body(ApiResponse.error(message));
        }

        /*
         * =========================
         * 403 - 권한 없음
         * =========================
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException e) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error("접근 권한이 없습니다."));
        }

        /*
         * =========================
         * 500 - 서버 에러
         * =========================
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<?>> handleServerError(Exception e) {
                log.error("Unhandled exception", e);

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("서버 오류가 발생했습니다."));
        }
}
