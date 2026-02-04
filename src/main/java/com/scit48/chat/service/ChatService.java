package com.scit48.chat.service;

import com.scit48.chat.domain.ChatMessage;
import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.domain.dto.ChatRoomDetailDto;
import com.scit48.chat.repository.ChatMessageRepository;
import com.scit48.chat.repository.ChatRoomRepository;
import com.scit48.chat.repository.ChatRoomMemberRepository;
import com.scit48.common.domain.entity.UserEntity; // 👈 UserDTO와 매핑되는 엔티티
import com.scit48.common.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 문자열 체크용

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
	
	private final ChatMessageRepository chatMessageRepository;
	private final RedisService redisService;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	
	// =================================================================
	// 1. 메시지 저장 (웹소켓 전송 시 호출)
	// =================================================================
	@Transactional
	public void saveMessage(ChatMessageDto messageDto) {
		ChatMessage chatMessage = ChatMessage.builder()
				.roomId(Long.parseLong(messageDto.getRoomId()))
				.senderId(messageDto.getSenderId())
				.senderMemberId(messageDto.getSenderMemberId())
				.senderNickname(messageDto.getSender())
				.content(messageDto.getMessage())
				.msgType(ChatMessage.MessageType.valueOf(messageDto.getType().name()))
				.build();
		
		chatMessageRepository.save(chatMessage);
		
		// Redis 활동량 기록
		if (messageDto.getSenderId() != null && messageDto.getReceiverId() != null) {
			redisService.recordInteraction(messageDto.getSenderId(), messageDto.getReceiverId());
		}
	}
	
	// =================================================================
	// 2. 지난 대화 목록 가져오기
	// =================================================================
	@Transactional(readOnly = true)
	public List<ChatMessageDto> getMessages(String roomId) {
		List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByMsgIdAsc(Long.parseLong(roomId));
		List<ChatMessageDto> dtos = new ArrayList<>();
		
		for (ChatMessage msg : messages) {
			ChatMessageDto dto = ChatMessageDto.builder()
					.roomId(String.valueOf(msg.getRoomId()))
					.senderId(msg.getSenderId())
					.senderMemberId(msg.getSenderMemberId())
					.sender(msg.getSenderNickname())
					.message(msg.getContent())
					.type(ChatMessageDto.MessageType.valueOf(msg.getMsgType().name()))
					.build();
			dtos.add(dto);
		}
		return dtos;
	}
	
	// =================================================================
	// 3. 채팅방 상세 정보 (사이드바용 상대방 프로필 조회)
	// =================================================================
	public ChatRoomDetailDto getRoomDetail(Long roomId, Long myId) {
		
		// 1) 채팅방 정보 조회
		ChatRoom room = chatRoomRepository.findById(roomId)
				.orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));
		
		// 2) 이 방의 참여자 중 '나(myId)'가 아닌 '상대방' 찾기
		Optional<UserEntity> opponentOpt = chatRoomMemberRepository.findOpponent(roomId, myId);
		
		// 3) 기본값 설정 (상대방이 나갔거나 없을 경우)
		Long oppId = 0L;
		String oppName = "(알 수 없음)";
		String oppNation = "";
		String oppIntro = "대화 상대가 없습니다.";
		String oppProfileImg = "/images/profile/default.png"; // 기본 이미지 경로
		Integer oppAge = null;
		
		// 4) 상대방 정보가 있다면 덮어쓰기
		if (opponentOpt.isPresent()) {
			UserEntity user = opponentOpt.get();
			
			oppId = user.getId();
			oppName = user.getNickname();
			oppIntro = user.getIntro();
			oppNation = user.getNation();  // ✅ UserDTO 필드 참고: DB의 nation 값
			oppAge = user.getAge();        // ✅ UserDTO 필드 참고: DB의 age 값
			
			// 프로필 이미지가 DB에 있으면 그것을 사용, 없으면 기본 이미지 유지
			if (StringUtils.hasText(user.getProfileImagePath())) {
				oppProfileImg = user.getProfileImagePath();
			}
		}
		
		// 5) DTO 생성 및 반환
		return ChatRoomDetailDto.builder()
				.roomId(roomId)
				.roomName(room.getName())
				.opponentId(oppId)
				.opponentNickname(oppName)
				.opponentNation(oppNation)
				.opponentIntro(oppIntro)
				.opponentProfileImg(oppProfileImg)
				.opponentAge(oppAge)
				.build();
	}
}