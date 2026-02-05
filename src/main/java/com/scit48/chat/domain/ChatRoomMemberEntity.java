package com.scit48.chat.domain;

import com.scit48.common.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
		name = "chat_room_member",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_room_user",
						columnNames = {"room_id", "user_id"}
				)
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatRoomMemberEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoom room;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
	@CreatedDate
	@Column(name = "joined_at", updatable = false)
	private LocalDateTime joinedAt;
	
	@Column(name = "room_name", length = 50)
	private String roomName;
	
	// ==========================================
	// 👇 [여기부터 추가] 필수 필드 및 메서드
	// ==========================================
	
	/**
	 * 마지막으로 읽은 메시지 ID
	 * - 안 읽은 메시지 판별의 기준점이 됩니다.
	 * - 기본값 0L (처음 들어오면 아무것도 안 읽은 상태)
	 */
	@Column(name = "last_read_msg_id")
	@Builder.Default // 빌더 패턴 사용 시 기본값 적용
	private Long lastReadMsgId = 0L;
	
	/**
	 * 읽은 위치 업데이트 메서드
	 * - 채팅방에 입장할 때 호출하여 lastReadMsgId를 최신값으로 변경합니다.
	 */
	public void updateLastReadMsgId(Long lastReadMsgId) {
		this.lastReadMsgId = lastReadMsgId;
	}
	
	// ==========================================
	
	public void updateRoomName(String roomName) {
		this.roomName = roomName;
	}
}