package com.scit48.chat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatNotifyDto {
	private Long roomId;
	private Long senderId;
}
