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
	
	/**
	 * room_id (FK)
	 * - room_id를 Long으로 들고 있어도 되지만,
	 *   조회/조인 편의를 위해 ManyToOne으로 매핑하는 것을 권장합니다.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoom room;
	
	/**
	 * user_id (FK)
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
	/**
	 * joined_at
	 * - DB가 DEFAULT CURRENT_TIMESTAMP라면 insert 시 자동으로 들어가긴 하지만,
	 *   JPA에서 엔티티 생성 시간 관리하려면 Auditing을 쓰는 편이 편합니다.
	 */
	@CreatedDate
	@Column(name = "joined_at", updatable = false)
	private LocalDateTime joinedAt;
	
	/**
	 * room_name (선택)
	 * - 사용자별 커스텀 방 이름
	 */
	@Column(name = "room_name", length = 50)
	private String roomName;
	
	// ====== 편의 메서드(선택) ======
	public void updateRoomName(String roomName) {
		this.roomName = roomName;
	}
}