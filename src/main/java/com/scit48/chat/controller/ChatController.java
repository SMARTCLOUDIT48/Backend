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
	
	// 1. 실시간 메시지 처리 (웹소켓)
	@MessageMapping("/chat/message")
	public void message(ChatMessageDto message) {
		
		// 임시 이름 처리
		if (message.getSender() == null) {
			message.setSender("익명" + message.getSenderId());
		}
		
		// 입장/퇴장 처리 (현재 접속자 수 관리)
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
		
		// 현재 방 인원수 로그
		long count = redisService.getUserCount(message.getRoomId());
		log.info("현재 방({}) 인원수: {}명", message.getRoomId(), count);
		
		// ★ 핵심: DB 저장 + Redis 활동량 기록 (ChatService 내부에서 처리됨)
		chatService.saveMessage(message);
		
		// 구독자들에게 메시지 발송
		messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
	}
	
	// 2. 채팅 내역 불러오기 (기존 기능)
	@GetMapping("/chat/history/{roomId}")
	@ResponseBody
	public List<ChatMessageDto> getChatHistory(@PathVariable String roomId) {
		return chatService.getMessages(roomId);
	}
	
	// 3. ✨ [추가된 기능] 상대방(또는 나)의 오늘 활동량 조회 API ✨
	// 요청 주소 예시: /chat/activity/100 (100번 유저가 오늘 몇 명과 대화했는지)
	@GetMapping("/chat/activity/{userId}")
	@ResponseBody
	public Long getUserActivity(@PathVariable Long userId) {
		return redisService.getTodayInteractionCount(userId);
	}
	
}