package com.scit48.chat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class DirectRoomResponseDTO {
	private Long roomId;
	private String roomUuid;
	private Long partnerId;
}
