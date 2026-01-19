package com.scit48.chat.controller;

import com.scit48.chat.dto.ChatMessageDto; // 아까 만든 DTO
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
	
	private final SimpMessageSendingOperations messagingTemplate;
	
	// 클라이언트가 /pub/chat/message 로 보내면 여기서 잡음
	@MessageMapping("/chat/message")
	public void message(ChatMessageDto message) {
		
		// 1. 아직 로그인 기능 없으니 임시 이름 세팅
		if (message.getSender() == null) {
			message.setSender("익명유저");
		}
		
		// 2. 받은 메시지를 /sub/chat/room/{roomId} 구독자들에게 다 쏴줌
		messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
	}
}