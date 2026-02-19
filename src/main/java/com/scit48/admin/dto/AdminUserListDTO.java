package com.scit48.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminUserListDTO {
	private Long userId;
	private String memberId;
	private String nickname;
	private String nation;
	private double manner;
	private LocalDateTime createdAt;
}
