package com.scit48.chat.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor  // JPA 필수
@AllArgsConstructor // Builder 사용 시 필수
@Builder            // 👈 이게 있어야 @Builder.Default가 작동합니다!
@Table(name = "chat_room")
public class ChatRoom {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id")
	private Long roomId;
	
	@Column(nullable = false)
	private String name;
	
	@Column(name = "room_uuid", unique = true, nullable = false)
	@Builder.Default // 👈 빌더로 만들 때도 UUID 자동 생성되도록 설정
	private String roomUuid = UUID.randomUUID().toString();
	
	// ==========================================
	// 👇 [추가] 스케줄러를 위한 필수 필드
	// ==========================================
	
	// 1. 방 생성 시간
	@Column(name = "created_at", updatable = false)
	@Builder.Default // 👈 빌더로 만들 때도 현재 시간 들어가도록 설정
	private LocalDateTime createdAt = LocalDateTime.now();
	
	// 2. 점수 반영 여부 (기본값 false)
	@Column(name = "is_evaluated")
	@Builder.Default // 👈 빌더로 만들 때 기본값(false) 적용
	private boolean isEvaluated = false;
	
	// ==========================================
	
	// ✅ 직접 생성자 호출(new ChatRoom(...))을 사용하는 경우를 위한 생성자
	public ChatRoom(String name) {
		this.name = name;
		this.roomUuid = UUID.randomUUID().toString();
		this.createdAt = LocalDateTime.now();
		this.isEvaluated = false;
	}
	
	// ✅ 스케줄러가 작업 후 호출할 메서드
	public void markAsEvaluated() {
		this.isEvaluated = true;
	}
}