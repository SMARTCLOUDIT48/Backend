package com.scit48.chat.service;

import com.scit48.chat.domain.ChatMessage;
import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.domain.ChatRoomMemberEntity;
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
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Slf4j
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
		
		// 2) ✅ 변경점: 방 멤버 리스트를 통째로 가져옴 (Repository 변경사항 반영)
		List<ChatRoomMemberEntity> members = chatRoomMemberRepository.findByChatRoomId(roomId);
		
		UserEntity opponent = null;
		
		// 3) ✅ 변경점: Java 반복문으로 안전하게 상대방 찾기
		// (리스트에서 내 아이디가 아닌 사람을 찾음)
		for (ChatRoomMemberEntity member : members) {
			if (!member.getUser().getId().equals(myId)) {
				opponent = member.getUser();
				break;
			}
		}
		
		// 4) 기본값 설정 (상대방 데이터가 꼬였거나 없을 때를 대비)
		Long oppId = 0L;
		String oppName = "(알 수 없음)";
		String oppNation = "Unknown";
		String oppIntro = "대화 상대가 없습니다.";
		String oppProfileImg = "/images/profile/default.png";
		Integer oppAge = null;
		
		// 5) 상대방 정보가 있다면 덮어쓰기
		if (opponent != null) {
			oppId = opponent.getId();
			oppName = opponent.getNickname();
			oppIntro = opponent.getIntro();
			oppNation = opponent.getNation();
			oppAge = opponent.getAge();
			
			if (StringUtils.hasText(opponent.getProfileImagePath())) {
				oppProfileImg = opponent.getProfileImagePath();
			}
		} else {
			// 로그를 남겨서 디버깅을 돕습니다.
			log.warn("⚠ 방번호 {}에서 상대방을 찾을 수 없음. (내 ID: {}, 멤버 수: {})", roomId, myId, members.size());
		}
		
		// 6) DTO 반환
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