package com.scit48.chat.service;

import com.scit48.chat.domain.ChatMessage;
import com.scit48.chat.dto.ChatMessageDto;
import com.scit48.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
	
	private final ChatMessageRepository chatMessageRepository;
	// 날짜 포맷 (예: 오후 2:30)
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
	
	@Transactional
	public void saveMessage(ChatMessageDto messageDto) {
		
		// 1. 현재 시간 구하기 (db 저장을 위해 날짜 객체 생성, 필요시)
		LocalDateTime now = LocalDateTime.now();
		
		// 2. DTO에 포맷팅된 시간 문자열 세팅 (화면 전송용) -> "오후 2:45"
		messageDto.setTime(now.format(formatter));
		
		ChatMessage chatMessage = ChatMessage.builder()
				.roomId(Long.parseLong(messageDto.getRoomId()))
				.senderId(messageDto.getSenderId()) // DTO의 senderId 사용
				.content(messageDto.getMessage())
				.type(ChatMessage.MessageType.valueOf(messageDto.getType().name()))
				.build();
		
		chatMessageRepository.save(chatMessage);
	}
	
	// 대화 내역 조회
	@Transactional(readOnly = true)
	public List<ChatMessageDto> getMessages(String roomId) {
		
		// ⭐ [수정된 부분] 바뀐 메서드 이름(findById...) 호출
		List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByIdAsc(Long.parseLong(roomId));
		
		List<ChatMessageDto> dtos = new ArrayList<>();
		
		for (ChatMessage msg : messages) {
			ChatMessageDto dto = ChatMessageDto.builder()
					.roomId(String.valueOf(msg.getRoomId()))
					.sender(String.valueOf(msg.getSenderId()))
					.message(msg.getContent())
					.type(ChatMessageDto.MessageType.valueOf(msg.getType().name()))
					.time(null)
					.build();
			dtos.add(dto);
		}
		
		return dtos;
	}
}