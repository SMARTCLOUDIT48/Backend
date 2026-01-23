package com.scit48.community.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
	private Long id;
	private Long boardId;      // 작성 시 어떤 게시글인지 지정
	private String content;    //
	
	// 작성자 정보
	private String writerNickname;
	private String writerProfileImage;
	
	private LocalDateTime createdDate;
}
