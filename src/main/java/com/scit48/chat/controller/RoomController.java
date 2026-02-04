package com.scit48.chat.controller;

import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.domain.dto.ChatRoomDetailDto; // 👈 DTO 패키지명 확인 필요
import com.scit48.chat.repository.ChatRoomRepository;
import com.scit48.chat.service.ChatService; // 👈 서비스 추가
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class RoomController {
	
	private final ChatRoomRepository chatRoomRepository;
	private final ChatService chatService; // ✅ [중요] 서비스 주입 (final 필수)
	
	// ==========================================
	// 1. 채팅방 목록 반환
	// ==========================================
	@GetMapping("/rooms")
	public List<ChatRoom> getRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
		if (userDetails == null) {
			return List.of();
		}
		Long myId = userDetails.getUser().getId();
		return chatRoomRepository.findMyChatRooms(myId);
	}
	
	// ==========================================
	// 2. [신규] 특정 채팅방 상세 정보 (상대방 프로필 등)
	// 요청 주소: /api/chat/room/{roomId}
	// ==========================================
	@GetMapping("/room/{roomId}")
	public ChatRoomDetailDto getRoomDetail(
			@PathVariable Long roomId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		if (userDetails == null) {
			throw new RuntimeException("로그인이 필요합니다.");
		}
		
		Long myId = userDetails.getUser().getId();
		
		// 서비스에서 상대방 정보를 찾아서 반환
		return chatService.getRoomDetail(roomId, myId);
	}
	
	// 3. 초기 데이터 생성 (테스트용)
	@GetMapping("/init")
	public String init() {
		List<String> roomNames = List.of("드라마 친구 🍿", "Alex (English) 🇺🇸", "개발자 모임 💻");
		for (String name : roomNames) {
			if (!chatRoomRepository.existsByName(name)) {
				chatRoomRepository.save(new ChatRoom(name));
			}
		}
		return "초기 데이터 체크 및 생성 완료!";
	}
}