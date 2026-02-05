package com.scit48.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminUserDetailDTO {
	private Long userId;
	private String memberId;
	private String nickname;
	private String gender;
	private int age;
	private String nation;
	private String intro;
	private String nativeLanguage;
	private String levelLanguage;
	private double manner;
	private LocalDateTime createdAt;
	private String profileImageUrl;
	
}
