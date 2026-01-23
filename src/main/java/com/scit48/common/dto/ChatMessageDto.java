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
	private String sender;         // 보낸 사람 닉네임 (화면 표시용)
	private Long senderId;         // 보낸 사람 고유 ID (DB 저장/구분용)
	
	// 👇 [여기 추가해주세요!] 👇
	private Long receiverId;       // 받는 사람 ID (Redis 활동량 집계용)
	// -------------------------
	
	private String message;        // 내용 (수정된 문장 포함)
	private String originalMessage;// 첨삭 시 원본 문장 (null 가능)
	private String time;           // 시간 문자열
	
	// (선택사항)
	// private int userCount;    // 현재 채팅방 인원 수
}