package com.scit48.community.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
	// 1. 공통/조회 필드
	private Long boardId;
	private String title;      //
	private String content;    //
	private int viewCount;     //
	private LocalDateTime createdDate;
	
	
	
	
	
	// 2. 카테고리 정보
	private Long categoryId;   // 작성 시 전달용
	private String categoryName; // 조회 시 반환용
	
	
	// 3. 작성자 정보 (User 연동)
	private Long id;
	private String memberId;
	private String writerNickname;     //
	private String profileImagePath;
	private String profileImageName;//
	private double manner;  //
	private String nation;
	
	
	
	
	// 4. 첨부 파일 정보
	private String fileName;
	private String fileOriginalName;
	private String filePath;
	
	// 5. 연관 데이터
	private List<CommentDTO> comments; // 댓글 목록
	private Integer likeCnt;           // 좋아요 총 개수
	private boolean liked;  // [추가] 내가 좋아요 눌렀는지 여부
}
