package com.scit48.community.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
	// 1. 공통/조회 필드
	private Long id;
	private String title;      //
	private String content;    //
	private int viewCount;     //
	private LocalDateTime createdDate;
	
	// 2. 카테고리 정보
	private Long categoryId;   // 작성 시 전달용
	private String categoryName; // 조회 시 반환용
	
	// 3. 작성자 정보 (Member 연동)
	private String writerNickname;     //
	private String writerProfileImage; //
	private Double writerMannerTemp;   //
	
	// 4. 첨부 파일 정보
	private String fileName;
	private String filePath;
	
	// 5. 연관 데이터
	private List<CommentDTO> comments; // 댓글 목록
	private int likeCount;             // 좋아요 총 개수
}
