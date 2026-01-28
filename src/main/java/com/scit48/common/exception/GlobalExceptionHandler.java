package com.scit48.common.exception;

import com.scit48.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
                                .findFirst()
                                .map(err -> err.getField() + ": " + err.getDefaultMessage())
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
         * 404 - 정적 리소스 없음 (중요!!)
         * =========================
         */
        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException e) {
                // default.png, favicon.ico 같은 요청은 조용히 404 처리
                return ResponseEntity.notFound().build();
        }

        /*
         * =========================
         * 500 - 서버 에러 (디버깅용)
         * =========================
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<?>> handleServerError(Exception e) {

                // 콘솔에 전체 스택트레이스 출력
                e.printStackTrace();

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error(
                                                e.getClass().getName() + " : " + e.getMessage()));
        }
}
