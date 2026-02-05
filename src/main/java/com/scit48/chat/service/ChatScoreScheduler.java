package com.scit48.chat.service;

import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.domain.ChatRoomMemberEntity;
import com.scit48.chat.repository.ChatMessageRepository;
import com.scit48.chat.repository.ChatRoomMemberRepository;
import com.scit48.chat.repository.ChatRoomRepository;
import com.scit48.common.domain.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatScoreScheduler {
	
	private final ChatRoomRepository chatRoomRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	
	// ✅ [변경 1] 실행 주기: 매 시간 정각마다 실행 (예: 1시 0분, 2시 0분...)
	@Scheduled(cron = "0 0 * * * *")
	@Transactional
	public void checkChatActivityAndPenalty() {
		log.info("⏰ [매너 점수 정산] 스케줄러 시작 (기준: 6시간)...");
		
		// ✅ [변경 2] 판단 기준: 생성된 지 6시간이 지난 방
		LocalDateTime limitTime = LocalDateTime.now().minusHours(6);
		
		// 6시간 지났고(Before limitTime), 아직 정산 안 된(False) 방 조회
		List<ChatRoom> targetRooms = chatRoomRepository.findByCreatedAtBeforeAndIsEvaluatedFalse(limitTime);
		
		if (targetRooms.isEmpty()) {
			log.info("   -> 정산할 채팅방이 없습니다.");
			return;
		}
		
		for (ChatRoom room : targetRooms) {
			long msgCount = chatMessageRepository.countByRoomId(room.getRoomId());
			
			if (msgCount < 5) {
				// 📉 5회 미만 -> 감점 (-0.1)
				log.info("📉 감점 대상: 방ID={}, 대화수={}", room.getRoomId(), msgCount);
				penaltyMembers(room.getRoomId());
			} else {
				// 📈 5회 이상 -> 가산점 (+0.1)
				log.info("📈 가산점 대상: 방ID={}, 대화수={}", room.getRoomId(), msgCount);
				rewardMembers(room.getRoomId());
			}
			
			// 정산 완료 처리 (중복 정산 방지)
			room.markAsEvaluated();
		}
	}
	
	// [감점 메서드]
	private void penaltyMembers(Long roomId) {
		List<ChatRoomMemberEntity> members = chatRoomMemberRepository.findByChatRoomId(roomId);
		for (ChatRoomMemberEntity member : members) {
			UserEntity user = member.getUser();
			user.decreaseManner(0.1);
			log.info("   -> [감점] 유저: {}, 현재점수: {}", user.getNickname(), user.getManner());
		}
	}
	
	// [가산점 메서드]
	private void rewardMembers(Long roomId) {
		List<ChatRoomMemberEntity> members = chatRoomMemberRepository.findByChatRoomId(roomId);
		for (ChatRoomMemberEntity member : members) {
			UserEntity user = member.getUser();
			user.increaseManner(0.1);
			log.info("   -> [가산] 유저: {}, 현재점수: {}", user.getNickname(), user.getManner());
		}
	}
}