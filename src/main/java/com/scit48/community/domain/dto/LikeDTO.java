package com.scit48.community.domain.dto;

import com.scit48.community.domain.entity.LikeKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeDTO {
	private Long boardId;  // 좋아요를 누른 게시글 ID
	private LikeKey likeId; // 좋아요를 누른 사용자 ID
	private Integer likeCnt;
	private LocalDateTime inputDate;
}
