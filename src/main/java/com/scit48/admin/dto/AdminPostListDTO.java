package com.scit48.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminPostListDTO {
	private String board;           // COMMUNITY / NOTICE / FAQ
	private Long postId;            // 글 ID (커뮤니티면 communityId, 공지면 noticeId)
	private String title;           // 제목
	private String authorNickname;  // 작성자 닉네임
	private LocalDateTime createdAt;
	private String detailUrl;       // 상세 이동 URL
}
