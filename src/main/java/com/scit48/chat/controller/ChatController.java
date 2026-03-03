package com.scit48.chat.controller;

import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.domain.ChatRoomMemberEntity;
import com.scit48.chat.repository.ChatRoomMemberRepository;
import com.scit48.chat.repository.ChatRoomRepository;
import com.scit48.chat.service.ChatService;
import com.scit48.chat.service.RedisService;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.dto.ChatMessageDto;
import com.scit48.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
	
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	
	/**
	 * 1. 채팅 페이지 접속
	 */
	@GetMapping("/chat")
	public String chatPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
		
		if (userDetails == null) {
			return "redirect:/login";
		}
		
		Long myId = userDetails.getUser().getId();
		
		// 내 방 목록(서버 렌더링용)
		List<ChatRoom> myRooms = chatRoomRepository.findMyChatRooms(myId);
		
		model.addAttribute("roomList", myRooms);
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
		Object userIdObj = (sessionAttributes != null) ? sessionAttributes.get("userId") : null;
		
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
		message.setTime(java.time.LocalDateTime.now().toString());
		
		if (ChatMessageDto.MessageType.ENTER.equals(message.getType())) {
			redisService.userEnter(message.getRoomId());
			message.setMessage(message.getSender() + "님이 입장하셨습니다.");
		} else if (ChatMessageDto.MessageType.QUIT.equals(message.getType())) {
			redisService.userLeave(message.getRoomId());
			message.setMessage(message.getSender() + "님이 퇴장하셨습니다.");
		}
		
		// ✅ 메시지 저장 (Redis + DB)
		redisService.saveMessageToRedis(message);
		chatService.saveMessage(message);
		
		// ✅ 현재 방 구독자들에게 메시지 전송
		messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
		
		// ==========================================================
		// ✅ [NEW] 실시간 🔴 알림: 상대방에게 notify 보내기
		// - TALK 메시지만 알림 보내도록(ENTER/QUIT 제외) 추천
		// ==========================================================
		if (ChatMessageDto.MessageType.TALK.equals(message.getType())) {
			
			// message.getRoomId()가 String이면 Long 변환 필요
			Long roomIdLong;
			try {
				roomIdLong = Long.parseLong(message.getRoomId());
			} catch (Exception e) {
				// 만약 roomId가 이미 Long 구조면 여기 들어오지 않음
				log.warn("⚠ roomId 파싱 실패: {}", message.getRoomId());
				return;
			}
			
			List<ChatRoomMemberEntity> members = chatRoomMemberRepository.findByChatRoomId(roomIdLong);
			
			for (ChatRoomMemberEntity m : members) {
				Long targetUserId = m.getUser().getId();
				
				// 발신자 제외(나한테는 알림 보낼 필요 없음)
				if (targetUserId.equals(userId)) continue;
				// 1. 내가 상대방과 대화했음을 기록
				redisService.recordInteraction(userId, targetUserId);
				redisService.recordInteraction(targetUserId, userId);
				
				Map<String, Object> payload = new HashMap<>();
				payload.put("roomId", roomIdLong);
				payload.put("senderId", userId);
				
				// 상대방 개인 알림 채널
				messagingTemplate.convertAndSend("/sub/chat/notify/" + targetUserId, payload);
			}
		}
	}
	
	@GetMapping("/chat/history/{roomId}")
	@ResponseBody
	public List<ChatMessageDto> getChatHistory(@PathVariable String roomId) {
		return redisService.getChatHistory(roomId);
	}
	
	@GetMapping("/chat/activity/{userId}")
	@ResponseBody
	public Long getUserActivity(@PathVariable Long userId) {
		return redisService.getTodayInteractionCount(userId);
	}
}
