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
	private String opponentNativeLanguage; // 상대방 모국어
	private String opponentStudyLanguage;  // 상대방 학습 언어
	private String opponentLevelLanguage;  // 상대방 언어 레벨 (Enum의 name)
	private Double opponentManner;         // 상대방 매너 점수
}
