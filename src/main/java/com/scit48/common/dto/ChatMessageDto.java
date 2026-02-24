package com.scit48.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
	
	// 메시지 타입 : 입장, 대화, 퇴장, 첨삭, 음성
	public enum MessageType {
		ENTER, TALK, QUIT, CORRECT, VOICE
	}
	
	private MessageType type;      // 메시지 타입
	private String roomId;         // 방 번호
	
	// ============================
	// 사용자 정보 (DB와 매칭)
	// ============================
	private String sender;         // 닉네임 (화면 표시용)
	private Long senderId;         // 유저 PK (user_id)
	
	// ⭐ [추가 필수] 컨트롤러 오류 해결용 (로그인 ID)
	private String senderMemberId; // 로그인 ID (예: test01)
	
	private String profileImageName;
	// ============================
	// 추가 기능
	// ============================
	private Long receiverId;       // 받는 사람 ID (활동량/귓속말용)
	
	private String message;        // 내용
	private String originalMessage;// 첨삭 시 원본 (null 가능)
	private String time;           // 시간
}