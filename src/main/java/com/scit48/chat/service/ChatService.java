package com.scit48.chat.service;

import com.scit48.chat.domain.ChatMessage;
import com.scit48.common.dto.ChatMessageDto;
import com.scit48.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
	
	private final ChatMessageRepository chatMessageRepository;
	private final RedisService redisService; // 👈 [추가 1] RedisService 주입
	
	@Transactional
	public void saveMessage(ChatMessageDto messageDto) {
		// 1. DB 저장용 엔티티 변환
		ChatMessage chatMessage = ChatMessage.builder()
				.roomId(Long.parseLong(messageDto.getRoomId()))
				.sender(messageDto.getSender())
				.senderId(messageDto.getSenderId())
				.content(messageDto.getMessage())
				.msgType(ChatMessage.MessageType.valueOf(messageDto.getType().name()))
				.build();
		
		// 2. MySQL에 대화 내용 저장
		chatMessageRepository.save(chatMessage);
		
		// 3. ✨ [추가 2] Redis에 "오늘 대화한 사람" 기록 ✨
		// 메시지가 정상적으로 저장되면, 보낸 사람의 활동 이력에 받는 사람을 추가합니다.
		if (messageDto.getSenderId() != null && messageDto.getReceiverId() != null) {
			redisService.recordInteraction(messageDto.getSenderId(), messageDto.getReceiverId());
		}
	}
	
	@Transactional(readOnly = true)
	public List<ChatMessageDto> getMessages(String roomId) {
		
		// 기존 로직 유지 (메시지 불러오기)
		List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByMsgIdAsc(Long.parseLong(roomId));
		
		List<ChatMessageDto> dtos = new ArrayList<>();
		
		for (ChatMessage msg : messages) {
			ChatMessageDto dto = ChatMessageDto.builder()
					.roomId(String.valueOf(msg.getRoomId()))
					.sender(msg.getSender())
					.senderId(msg.getSenderId())
					.message(msg.getContent())
					.type(ChatMessageDto.MessageType.valueOf(msg.getMsgType().name()))
					.build();
			dtos.add(dto);
		}
		
		return dtos;
	}
}