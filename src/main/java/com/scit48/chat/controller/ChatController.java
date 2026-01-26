package com.scit48.chat.controller;

import com.scit48.common.domain.entity.UserEntity; // ✅ 엔티티 경로
import com.scit48.common.repository.UserRepository; // ✅ 레포지토리 경로
import com.scit48.common.dto.ChatMessageDto;
import com.scit48.chat.service.ChatService;
import com.scit48.chat.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {
	
	private final SimpMessageSendingOperations messagingTemplate;
	private final ChatService chatService;
	private final RedisService redisService;
	
	// ✅ 공통 레포지토리 주입
	private final UserRepository userRepository;
	
	/**
	 * 실시간 메시지 처리 (웹소켓)
	 */
	@MessageMapping("/chat/message")
	public void message(ChatMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
		
		// 1. 세션에서 userId(PK) 꺼내기 (Long 타입)
		Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
		Long userId = (Long) sessionAttributes.get("userId");
		
		if (userId == null) {
			log.error("❌ 웹소켓 세션에 유저 정보가 없습니다. (비로그인 상태)");
			// 필요시 여기서 예외를 던지거나 return으로 종료
			return;
		}
		
		// 2. DB에서 실제 유저 조회
		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다. PK: " + userId));
		
		// 3. 🚨 데이터 위조 방지: DB 정보로 덮어쓰기 (Entity 필드명에 맞춤)
		
		// ⭐ [수정됨] Entity 필드가 "private Long id;" 이므로 getId() 사용!
		message.setSenderId(user.getId());
		
		// Entity 필드가 "private String memberId;" 이므로 getMemberId() 사용
		message.setSenderMemberId(user.getMemberId());
		
		// Entity 필드가 "private String nickname;" 이므로 getNickname() 사용
		message.setSender(user.getNickname());
		
		
		// --- 이하 로직 동일 ---
		
		// 입장/퇴장 처리
		if (ChatMessageDto.MessageType.ENTER.equals(message.getType())) {
			redisService.userEnter(message.getRoomId());
			message.setMessage(message.getSender() + "님이 입장하셨습니다.");
			log.info("입장: {} (방: {})", message.getSender(), message.getRoomId());
		}
		else if (ChatMessageDto.MessageType.QUIT.equals(message.getType())) {
			redisService.userLeave(message.getRoomId());
			message.setMessage(message.getSender() + "님이 퇴장하셨습니다.");
			log.info("퇴장: {} (방: {})", message.getSender(), message.getRoomId());
		}
		
		// DB 저장 및 전송
		chatService.saveMessage(message);
		messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
	}
	
	// 기존 기능 유지
	@GetMapping("/chat/history/{roomId}")
	@ResponseBody
	public List<ChatMessageDto> getChatHistory(@PathVariable String roomId) {
		return chatService.getMessages(roomId);
	}
	
	@GetMapping("/chat/activity/{userId}")
	@ResponseBody
	public Long getUserActivity(@PathVariable Long userId) {
		return redisService.getTodayInteractionCount(userId);
	}
}