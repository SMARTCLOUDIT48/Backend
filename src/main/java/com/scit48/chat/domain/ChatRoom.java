package com.scit48.chat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor
public class ChatRoom {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "room_id") // 👈 이 줄을 추가해서 DB 컬럼명과 매핑해줍니다!
	private Long id;
	
	private String name;
	
	@Column(name = "room_uuid") // 기왕 하는 김에 이것도 명시해주면 좋습니다.
	private String roomUuid;
	
	public ChatRoom(String name) {
		this.name = name;
		this.roomUuid = UUID.randomUUID().toString();
	}
}