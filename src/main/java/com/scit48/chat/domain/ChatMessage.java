package com.scit48.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message")
public class ChatMessage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "msg_id")
	private Long id;
	
	@Column(name = "room_id")
	private Long roomId;    // (편의상 ID로 바로 저장)
	
	@Column(name = "sender_id")
	private Long senderId;  // (보낸 사람 PK)
	
	@Column(columnDefinition = "TEXT")
	private String content; // 내용
	
	@Column(name = "msg_type")
	@Enumerated(EnumType.STRING)
	private MessageType type; // TALK, ENTER, QUIT
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	// 생성자 (빌더 패턴)
	@Builder
	public ChatMessage(Long roomId, Long senderId, String content, MessageType type) {
		this.roomId = roomId;
		this.senderId = senderId;
		this.content = content;
		this.type = type;
	}
	
	public enum MessageType {
		ENTER, TALK, QUIT
	}
}