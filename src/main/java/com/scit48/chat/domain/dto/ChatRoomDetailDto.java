package com.scit48.chat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDetailDto {
	
	private Long roomId;
	private String roomName;
	
	// 프론트엔드 사이드바에 표시할 상대방 정보
	private Long opponentId;          // 상대방 PK
	private String opponentNickname;  // 닉네임
	private String opponentProfileImg;// 프로필 사진 경로
	private String opponentNation;    // 국적
	private String opponentIntro;     // 자기소개
	private Integer opponentAge;      // 나이
}