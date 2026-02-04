package com.scit48.chat.controller;

import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.chat.domain.ChatRoom; // ✅ Import 확인
import com.scit48.chat.repository.ChatRoomRepository; // ✅ Import 확인
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.repository.UserRepository;
import com.scit48.common.dto.ChatMessageDto;
import com.scit48.chat.service.ChatService;
import com.scit48.chat.service.RedisService;

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
	
	// ✅ 추가: 방 목록을 불러오기 위해 Repository 주입
	private final ChatRoomRepository chatRoomRepository;
	
	/**
	 * 1. 채팅 페이지 접속
	 */
	@GetMapping("/chat")
	public String chatPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
		
		// 로그인 안 된 경우 로그인 페이지로 리다이렉트
		if (userDetails == null) {
			return "redirect:/login";
		}
		
		Long myId = userDetails.getUser().getId();
		
		// ✅ [핵심 수정] 내 방만 가져와서 화면으로 전달!
		// 이전에 Repository에 만든 메서드 사용 (findAll() 아님)
		List<ChatRoom> myRooms = chatRoomRepository.findMyChatRooms(myId);
		
		model.addAttribute("roomList", myRooms); // HTML에서 th:each="room : ${roomList}"로 씀
		model.addAttribute("myUserId", myId);
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
		
		// ✅ 메시지 저장 (Redis + DB)
		// RedisService의 저장 메서드를 호출해야 채팅이 안 섞입니다!
		// (현재 코드는 chatService.saveMessage만 호출 중인데, RedisService도 호출하는 게 좋습니다)
		redisService.saveMessageToRedis(message);
		chatService.saveMessage(message); // DB 저장용이라면 유지
		
		messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
	}
	
	@GetMapping("/chat/history/{roomId}")
	@ResponseBody
	public List<ChatMessageDto> getChatHistory(@PathVariable String roomId) {
		// ✅ Redis에서 먼저 가져오도록 수정 (속도 향상 + 방 분리 확실)
		return redisService.getChatHistory(roomId);
	}
	
	@GetMapping("/chat/activity/{userId}")
	@ResponseBody
	public Long getUserActivity(@PathVariable Long userId) {
		return redisService.getTodayInteractionCount(userId);
	}
}