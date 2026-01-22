package com.scit48.chat.controller;

import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class RoomController {
	
	private final ChatRoomRepository chatRoomRepository;
	
	// 1. 채팅방 목록 반환
	@GetMapping("/rooms")
	public List<ChatRoom> getRooms() {
		return chatRoomRepository.findAll();
	}
	
	// 2. 초기 데이터 생성 (중복 방지 로직 적용)
	@GetMapping("/init")
	public String init() {
		List<String> roomNames = List.of("드라마 친구 🍿", "Alex (English) 🇺🇸", "개발자 모임 💻");
		
		for (String name : roomNames) {
			// 방이 없을 때만 생성! (이제 에러 안 남)
			if (!chatRoomRepository.existsByName(name)) {
				chatRoomRepository.save(new ChatRoom(name));
			}
		}
		return "초기 데이터 체크 및 생성 완료!";
	}
}