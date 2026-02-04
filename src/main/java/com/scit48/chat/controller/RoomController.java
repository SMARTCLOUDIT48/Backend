package com.scit48.chat.controller;

import com.scit48.auth.member.service.CustomUserDetails; // ✅ 추가
import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // ✅ 추가
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class RoomController {
	
	private final ChatRoomRepository chatRoomRepository;
	
	// ==========================================
	// 1. 채팅방 목록 반환 (여기가 범인이었음!)
	// ==========================================
	@GetMapping("/rooms")
	public List<ChatRoom> getRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		// 로그인 정보가 없으면 빈 목록 반환 (혹은 에러 처리)
		if (userDetails == null) {
			return List.of();
		}
		
		Long myId = userDetails.getUser().getId();
		
		// 🚨 기존: findAll() -> 모든 방을 다 가져옴 (삭제)
		// return chatRoomRepository.findAll();
		
		// ✅ 수정: 내(myId)가 참여 중인 방만 가져옴!
		return chatRoomRepository.findMyChatRooms(myId);
	}
	
	// 2. 초기 데이터 생성 (기존 유지)
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