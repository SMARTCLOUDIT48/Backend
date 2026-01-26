package com.scit48.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "chat_room")
public class ChatRoom {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id")
	private Long roomId; // 👈 id -> roomId로 변경 (Lombok 에러 방지)
	
	@Column(nullable = false)
	private String name;
	
	@Column(name = "room_uuid", unique = true, nullable = false)
	private String roomUuid;
	
	public ChatRoom(String name) {
		this.name = name;
		this.roomUuid = UUID.randomUUID().toString();
	}
}