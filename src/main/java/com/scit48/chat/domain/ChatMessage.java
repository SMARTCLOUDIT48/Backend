package com.scit48.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Builder                // ★ 이게 없어서 에러가 났던 겁니다!
@NoArgsConstructor
@AllArgsConstructor     // ★ @Builder를 쓰려면 이것도 꼭 필요합니다.
@Table(name = "chat_message")
public class ChatMessage {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "msg_id")
	private Long msgId;
	
	// Service에서 Long으로 변환해서 쓰므로 Long으로 통일
	@Column(name = "room_id")
	private Long roomId;
	
	private String sender; // 보낸 사람 이름
	
	@Column(name = "sender_id")
	private Long senderId;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	public enum MessageType {
		ENTER, TALK, QUIT, CORRECT, VOICE
	}
	
	// DB 컬럼명 msg_type과 매핑
	@Enumerated(EnumType.STRING)
	@Column(name = "msg_type")
	private MessageType msgType;
	
	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}