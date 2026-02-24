package com.scit48.chat.service;

import com.scit48.chat.domain.ChatMessage;
import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.domain.ChatRoomMemberEntity;
import com.scit48.chat.domain.dto.ChatRoomDetailDto;
import com.scit48.chat.domain.dto.ChatRoomListDto;
import com.scit48.chat.repository.ChatMessageRepository;
import com.scit48.chat.repository.ChatRoomRepository;
import com.scit48.chat.repository.ChatRoomMemberRepository;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
		// 1. 메시지 DB 저장
		ChatMessage chatMessage = ChatMessage.builder()
				.roomId(Long.parseLong(messageDto.getRoomId()))
				.senderId(messageDto.getSenderId())
				.senderMemberId(messageDto.getSenderMemberId())
				.senderNickname(messageDto.getSender())
				.content(messageDto.getMessage())
				.msgType(ChatMessage.MessageType.valueOf(messageDto.getType().name()))
				.build();
		
		ChatMessage savedMsg = chatMessageRepository.save(chatMessage);
		
		// 2. Redis 활동량 기록
		if (messageDto.getSenderId() != null && messageDto.getReceiverId() != null) {
			redisService.recordInteraction(messageDto.getSenderId(), messageDto.getReceiverId());
		}
		
		// 3. ✅ [추가] 내가 보낸 메시지이므로, 내 '마지막 읽은 ID'도 즉시 업데이트
		// (이걸 안 하면 내가 보낸 메시지가 '안 읽은 메시지'로 카운트됨)
		chatRoomMemberRepository.findMyMembership(messageDto.getSenderId(), Long.parseLong(messageDto.getRoomId()))
				.ifPresent(member -> {
					member.updateLastReadMsgId(savedMsg.getMsgId());
				});
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
		
		// 2) 방 멤버 리스트 조회
		List<ChatRoomMemberEntity> members = chatRoomMemberRepository.findByChatRoomId(roomId);
		
		UserEntity opponent = null;
		
		// 3) 상대방 찾기
		for (ChatRoomMemberEntity member : members) {
			if (member.getUser() != null && !member.getUser().getId().equals(myId)) {
				opponent = member.getUser();
				break;
			}
		}
		
		// 4) 기본값 설정
		Long oppId = 0L;
		String oppName = "(알 수 없음)";
		String oppNation = "Unknown";
		String oppIntro = "대화 상대가 없습니다.";
		String oppProfileImg = "/images/profile/";
		String oppProfileImgName = "default.png";
		Integer oppAge = null;
		Double oppManner = null;
		
		// ✨ [추가] 언어 관련 기본값 설정
		String oppNativeLanguage = null;
		String oppStudyLanguage = null;
		String oppLevelLanguage = null;
		
		// 5) 상대방 정보 세팅
		if (opponent != null) {
			oppId = opponent.getId();
			oppName = opponent.getNickname();
			oppIntro = opponent.getIntro();
			oppNation = opponent.getNation();
			oppAge = opponent.getAge();
			oppManner = opponent.getManner();
			
			if (StringUtils.hasText(opponent.getProfileImagePath())) {
				oppProfileImg = opponent.getProfileImagePath();
				oppProfileImgName = opponent.getProfileImageName();
			}
			
			// ✨ [추가] 상대방 언어 및 레벨 정보 추출
			oppNativeLanguage = opponent.getNativeLanguage();
			oppStudyLanguage = opponent.getStudyLanguage();
			// Enum 타입일 경우 name()으로 문자열 변환 (null 체크 필수)
			if (opponent.getLevelLanguage() != null) {
				oppLevelLanguage = opponent.getLevelLanguage().name();
			}
			
		} else {
			log.warn("⚠ 방번호 {}에서 상대방을 찾을 수 없음. (내 ID: {}, 멤버 수: {})",
					roomId, myId, members.size());
		}
		
		// 6) DTO 변환 및 반환
		return ChatRoomDetailDto.builder()
				.roomId(roomId)
				.roomName(room.getName())
				.opponentId(oppId)
				.opponentNickname(oppName)
				.opponentNation(oppNation)
				.opponentIntro(oppIntro)
				.opponentProfileImg(oppProfileImg)
				.opponentProfileImgName(oppProfileImgName)
				.opponentAge(oppAge)
				.opponentManner(oppManner)
				// ✨ [추가] DTO에 언어/레벨 데이터 꽂아주기
				.opponentNativeLanguage(oppNativeLanguage)
				.opponentStudyLanguage(oppStudyLanguage)
				.opponentLevelLanguage(oppLevelLanguage)
				.build();
	}
	
	// =================================================================
	// 4. 채팅방 목록 조회 (🔴 안 읽은 메시지 여부 포함)
	// =================================================================
	@Transactional(readOnly = true)
	public List<ChatRoomListDto> getMyChatRoomsWithUnread(Long userId) {
		
		// 1️⃣ 내가 속한 모든 방 멤버십 가져오기
		List<ChatRoomMemberEntity> memberships =
				chatRoomMemberRepository.findMyMemberships(userId);
		
		// 2️⃣ roomId -> lastReadMsgId 맵핑 (NULL 방지 및 중복 시 최신값)
		Map<Long, Long> lastReadMap = memberships.stream()
				.filter(m -> m.getRoom() != null)
				.filter(m -> m.getRoom().getRoomId() != null)
				.collect(Collectors.toMap(
						m -> m.getRoom().getRoomId(),
						m -> m.getLastReadMsgId() == null ? 0L : m.getLastReadMsgId(),
						Math::max
				));
		
		// 3️⃣ 실제 방 목록 가져오기
		List<ChatRoom> rooms = chatRoomRepository.findMyChatRooms(userId);
		
		// 4️⃣ 안 읽음 여부(hasUnread) 계산하여 DTO 변환
		List<ChatRoomListDto> result = new ArrayList<>();
		
		for (ChatRoom room : rooms) {
			Long roomId = room.getRoomId();
			
			// 방의 가장 최신 메시지 ID 조회
			Long lastMsgId = chatMessageRepository.findLastMessageId(roomId);
			if (lastMsgId == null) lastMsgId = 0L;
			
			// 내가 읽은 마지막 메시지 ID
			Long lastReadMsgId = lastReadMap.getOrDefault(roomId, 0L);
			
			// 안 읽은 메시지가 있는지 판단
			boolean hasUnread = lastMsgId > lastReadMsgId;
			
			result.add(ChatRoomListDto.builder()
					.roomId(roomId)
					.roomName(room.getName())
					.hasUnread(hasUnread)
					.build());
		}
		
		return result;
	}
	
	// =================================================================
	// 5. 채팅방 읽음 처리 (입장 시 lastReadMsgId 최신으로 갱신)
	// =================================================================
	@Transactional
	public void markAsRead(Long roomId, Long userId) {
		
		// 내 멤버십 정보 조회
		ChatRoomMemberEntity member = chatRoomMemberRepository
				.findMyMembership(userId, roomId)
				.orElseThrow(() -> new RuntimeException("채팅방 멤버 정보를 찾을 수 없습니다."));
		
		// 현재 방의 최신 메시지 ID 조회
		Long lastMsgId = chatMessageRepository.findLastMessageId(roomId);
		if (lastMsgId == null) lastMsgId = 0L;
		
		// 내 위치 업데이트
		member.updateLastReadMsgId(lastMsgId);
		
		// JPA Dirty Checking으로 자동 저장되지만, 명시적 저장도 안전함
		chatRoomMemberRepository.save(member);
	}
}