package com.scit48.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * 400 - 잘못된 요청
         * (비즈니스 로직 오류, 파라미터 오류)
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(
                        IllegalArgumentException e) {
                return ResponseEntity
                                .badRequest()
                                .body(new ErrorResponse(
                                                "BAD_REQUEST",
                                                e.getMessage()));
        }

        /**
         * 401 - 인증 실패
         * (로그인 실패, 이메일 미인증 등)
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorized(
                        IllegalStateException e) {
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(new ErrorResponse(
                                                "UNAUTHORIZED",
                                                e.getMessage()));
        }

        /**
         * 400 - Validation 실패
         * (@Valid DTO 검증 실패)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(
                        MethodArgumentNotValidException e) {

                String message = e.getBindingResult()
                                .getFieldErrors()
                                .get(0)
                                .getDefaultMessage();

                return ResponseEntity
                                .badRequest()
                                .body(new ErrorResponse(
                                                "VALIDATION_ERROR",
                                                message));
        }

        /**
         * 403 - 권한 없음
         * (로그인 O, 권한 X)
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(
                        AccessDeniedException e) {
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(new ErrorResponse(
                                                "FORBIDDEN",
                                                "접근 권한이 없습니다."));
        }

        /**
         * 500 - 서버 에러
         * (예상 못 한 모든 예외)
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleServerError(
                        Exception e) {
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ErrorResponse(
                                                "INTERNAL_ERROR",
                                                "서버 오류가 발생했습니다."));
        }
}
