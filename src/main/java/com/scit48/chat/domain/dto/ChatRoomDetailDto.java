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
	
	// 상대방 정보
	private Long opponentId;
	private String opponentNickname;
	private String opponentProfileImg;
	private String opponentNation;
	private String opponentIntro;
	private Integer opponentAge;
	
	// ✅ 매너 점수 (이게 없어서 안 뜬 것)
	private Double opponentManner;
}