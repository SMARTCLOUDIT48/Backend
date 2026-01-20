package com.scit48.chat.controller;

import com.scit48.common.dto.ChatMessageDto;
import com.scit48.chat.service.ChatService;
import com.scit48.chat.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
	
	private final SimpMessageSendingOperations messagingTemplate;
	private final ChatService chatService; // DB 저장용
	private final RedisService redisService; // 인원수 카운팅용
	
	@MessageMapping("/chat/message")
	public void message(ChatMessageDto message) {
		
		// 1. 로그인 기능 없으므로 임시 사용자 이름 처리
		if (message.getSender() == null) {
			message.setSender("익명" + message.getSenderId());
		}
		
		// 2. 메시지 타입별 로직 (Redis 인원수 체크)
		if (ChatMessageDto.MessageType.ENTER.equals(message.getType())) {
			redisService.userEnter(message.getRoomId());
			message.setMessage(message.getSender() + "님이 입장하셨습니다.");
			log.info("사용자 입장: 방 번호 {}", message.getRoomId());
		}
		else if (ChatMessageDto.MessageType.QUIT.equals(message.getType())) {
			redisService.userLeave(message.getRoomId());
			message.setMessage(message.getSender() + "님이 퇴장하셨습니다.");
			log.info("사용자 퇴장: 방 번호 {}", message.getRoomId());
		}
		
		// 3. 현재 인원수 가져오기 (로그 확인용)
		long count = redisService.getUserCount(message.getRoomId());
		log.info("현재 방({}) 인원수: {}명", message.getRoomId(), count);
		
		// (선택 사항) 클라이언트에게 인원수 정보를 같이 보내고 싶다면 DTO에 userCount 필드를 추가해서 담아 보내면 됩니다.
		// message.setUserCount(count);
		
		// 4. DB에 저장 (모든 메시지 기록)
		chatService.saveMessage(message);
		
		// 5. 구독자들에게 메시지 발송
		messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
	}
	
	@GetMapping("/chat/history/{roomId}")
	@ResponseBody
	public List<ChatMessageDto> getChatHistory(@PathVariable String roomId) {
		return chatService.getMessages(roomId);
	}
}