package com.scit48.community.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeDTO {
	private Long boardId;  // 좋아요를 누른 게시글 ID
	private Long likeId; // 좋아요를 누른 사용자 ID
	private boolean status; // 현재 좋아요 상태 (T/F) - 프론트엔드 처리용
}
