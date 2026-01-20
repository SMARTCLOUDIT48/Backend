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
	
	@Transactional
	public void saveMessage(ChatMessageDto messageDto) {
		ChatMessage chatMessage = ChatMessage.builder()
				.roomId(Long.parseLong(messageDto.getRoomId()))
				.sender(messageDto.getSender())
				.senderId(messageDto.getSenderId())
				.content(messageDto.getMessage())
				.msgType(ChatMessage.MessageType.valueOf(messageDto.getType().name()))
				.build();
		
		chatMessageRepository.save(chatMessage);
	}
	
	@Transactional(readOnly = true)
	public List<ChatMessageDto> getMessages(String roomId) {
		
		// ★ [수정] 메서드 이름 변경 (Id -> MsgId)
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