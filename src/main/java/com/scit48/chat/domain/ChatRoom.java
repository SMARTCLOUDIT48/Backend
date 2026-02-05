package com.scit48.chat.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime; // 날짜 타입 추가
import java.util.UUID;


@Entity
@Getter @Setter
@NoArgsConstructor // JPA 필수
@AllArgsConstructor // Builder 사용 시 필수
@Builder
@Table(name = "chat_room")
public class ChatRoom {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id")
	private Long roomId;
	
	@Column(nullable = false)
	private String name;
	
	@Column(name = "room_uuid", unique = true, nullable = false)
	private String roomUuid;
	
	// ==========================================
	// 👇 [추가] 스케줄러를 위한 필수 필드 2개
	// ==========================================
	
	// 1. 방 생성 시간 (24시간 지났는지 확인용)
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	// 2. 점수 반영 여부 (중복 감점 방지용)
	// 기본값 false로 설정
	@Column(name = "is_evaluated") // ✅ 이렇게만 적어도 됨
	@Builder.Default
	private boolean isEvaluated = false;
	
	// ==========================================
	
	public ChatRoom(String name) {
		this.name = name;
		this.roomUuid = UUID.randomUUID().toString();
		this.createdAt = LocalDateTime.now(); // ✅ 생성 시 현재 시간 자동 기록
	}
	
	// ✅ 스케줄러가 작업 후 호출할 메서드
	public void markAsEvaluated() {
		this.isEvaluated = true;
	}
}