package com.scit48.chat.controller;

import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.repository.UserRepository;
import com.scit48.common.dto.ChatMessageDto;
import com.scit48.chat.service.ChatService;
import com.scit48.chat.service.RedisService;
import com.scit48.auth.member.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
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
	private final UserRepository userRepository;
	
	/**
	 * 1. 채팅 페이지 접속
	 * 🚨 아래 파라미터의 CustomUserDetails가 빨간색이면 Alt+Enter 눌러서 Import class 하세요!
	 */
	@GetMapping("/chat")
	public String chatPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
		
		// 로그인 안 된 경우 로그인 페이지로 리다이렉트
		if (userDetails == null) {
			return "redirect:/login";
		}
		
		// 내 정보 전달
		model.addAttribute("myUserId", userDetails.getUser().getId());
		model.addAttribute("myNickname", userDetails.getUser().getNickname());
		
		return "chat";
	}
	
	/**
	 * 2. 실시간 메시지 처리
	 */
	@MessageMapping("/chat/message")
	public void message(ChatMessageDto message, SimpMessageHeaderAccessor headerAccessor) {
		
		Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
		Object userIdObj = sessionAttributes.get("userId");
		
		if (userIdObj == null) {
			log.error("❌ 웹소켓 세션에 유저 정보가 없습니다.");
			return;
		}
		
		Long userId = Long.parseLong(userIdObj.toString());
		
		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("유저 없음: " + userId));
		
		message.setSenderId(user.getId());
		message.setSenderMemberId(user.getMemberId());
		message.setSender(user.getNickname());
		
		if (ChatMessageDto.MessageType.ENTER.equals(message.getType())) {
			redisService.userEnter(message.getRoomId());
			message.setMessage(message.getSender() + "님이 입장하셨습니다.");
		}
		else if (ChatMessageDto.MessageType.QUIT.equals(message.getType())) {
			redisService.userLeave(message.getRoomId());
			message.setMessage(message.getSender() + "님이 퇴장하셨습니다.");
		}
		
		chatService.saveMessage(message);
		messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
	}
	
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