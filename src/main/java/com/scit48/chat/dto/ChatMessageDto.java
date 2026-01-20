package com.scit48.chat.dto;

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
	
	// 메시지 타입 : 입장, 대화, 퇴장
	public enum MessageType {
		ENTER, TALK, QUIT
	}
	
	private MessageType type; // 메시지 타입
	private String roomId;    // 방 번호
	private String sender;    // 보낸 사람 (닉네임)
	private String message;   // 메시지 내용
	private String time;      // 보낸 시간 (예: "오후 2:30")
	private Long senderId;
	// (선택사항) 나중에 추가될 수 있는 필드들
	// private Long userId;      // DB 연동용 유저 ID (온도 깎을 때 필요)
	// private int userCount;    // 현재 채팅방 인원 수
}