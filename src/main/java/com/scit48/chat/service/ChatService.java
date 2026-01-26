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
	private final RedisService redisService;
	
	@Transactional
	public void saveMessage(ChatMessageDto messageDto) {
		// 1. DTO -> Entity 변환 (DB 저장용)
		ChatMessage chatMessage = ChatMessage.builder()
				.roomId(Long.parseLong(messageDto.getRoomId()))
				
				// 🌟 [핵심 변경] 새로 추가된 필드 매핑
				.senderId(messageDto.getSenderId())           // PK (user_id)
				.senderMemberId(messageDto.getSenderMemberId()) // 로그인 ID (test01)
				.senderNickname(messageDto.getSender())       // 닉네임 (화면 표시용)
				
				.content(messageDto.getMessage())
				.msgType(ChatMessage.MessageType.valueOf(messageDto.getType().name()))
				.build();
		
		// 2. MySQL에 대화 내용 저장
		chatMessageRepository.save(chatMessage);
		
		// 3. Redis 활동량 기록
		if (messageDto.getSenderId() != null && messageDto.getReceiverId() != null) {
			redisService.recordInteraction(messageDto.getSenderId(), messageDto.getReceiverId());
		}
	}
	
	@Transactional(readOnly = true)
	public List<ChatMessageDto> getMessages(String roomId) {
		
		// DB에서 메시지 가져오기
		List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByMsgIdAsc(Long.parseLong(roomId));
		
		List<ChatMessageDto> dtos = new ArrayList<>();
		
		for (ChatMessage msg : messages) {
			// Entity -> DTO 변환 (화면 출력용)
			ChatMessageDto dto = ChatMessageDto.builder()
					.roomId(String.valueOf(msg.getRoomId()))
					
					// 🌟 [핵심 변경] DB에서 꺼낸 정보 다시 DTO에 담기
					.senderId(msg.getSenderId())
					.senderMemberId(msg.getSenderMemberId()) // 로그인 ID
					.sender(msg.getSenderNickname())         // 닉네임
					
					.message(msg.getContent())
					.type(ChatMessageDto.MessageType.valueOf(msg.getMsgType().name()))
					.build();
			dtos.add(dto);
		}
		
		return dtos;
	}
}