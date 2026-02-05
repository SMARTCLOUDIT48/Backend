package com.scit48.chat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListDto {
	
	private Long roomId;
	private String roomName;
	
	// ✅ 빨간 점 표시용
	private boolean hasUnread;
}
