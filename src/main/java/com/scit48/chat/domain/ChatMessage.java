package com.scit48.chat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_message")
public class ChatMessage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "msg_id")
	private Long msgId;
	
	@Column(name = "room_id")
	private Long roomId;
	
	// ==========================================
	// 👇 [중요] 사용자 정보 컬럼 추가 (DB 저장용)
	// ==========================================
	@Column(name = "sender_id")
	private Long senderId;         // 유저 PK (예: 1)
	
	@Column(name = "sender_member_id")
	private String senderMemberId; // 로그인 ID (예: test01)
	
	@Column(name = "sender_nickname")
	private String senderNickname; // 화면 표시 이름 (예: 홍길동)
	// ==========================================
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	// 메시지 타입 (ENTER, TALK 등)
	@Enumerated(EnumType.STRING)
	@Column(name = "msg_type")
	private MessageType msgType;
	
	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	// Enum 정의 (DTO와 맞춰줌)
	public enum MessageType {
		ENTER, TALK, QUIT, CORRECT, VOICE
	}
	
	// 빌더 패턴 (Service에서 저장할 때 사용)
	@Builder
	public ChatMessage(Long roomId, Long senderId, String senderMemberId, String senderNickname, String content, MessageType msgType) {
		this.roomId = roomId;
		this.senderId = senderId;
		this.senderMemberId = senderMemberId;
		this.senderNickname = senderNickname;
		this.content = content;
		this.msgType = msgType;
	}
}