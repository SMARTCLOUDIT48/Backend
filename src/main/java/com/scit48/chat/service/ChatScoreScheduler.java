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
	
	// ✅ 1시간마다 실행(cron = "0 0 * * * *"), (테스트할 땐 "0 */1 * * * *" 로 1분마다 설정 가능)
	@Scheduled(cron = "0 */1 * * * *")
	@Transactional // 트랜잭션이 끝나면 변경된 점수가 DB에 자동 저장됨 (Dirty Checking)
	public void checkChatActivityAndPenalty() {
		log.info("⏰ [매너 점수 정산] 스케줄러 시작...");
		
		// 1. 기준 시간: 현재 시간 - 24시간minusHours(24),  테스트용 minusSeconds(60); 1분마다 체크하여 감점
		LocalDateTime limitTime = LocalDateTime.now().minusSeconds(60);
		
		// 2. 조건에 맞는 방 조회 (24시간 지남 + 정산 안 함)
		List<ChatRoom> targetRooms = chatRoomRepository.findByCreatedAtBeforeAndIsEvaluatedFalse(limitTime);
		
		if (targetRooms.isEmpty()) {
			log.info("   -> 정산할 채팅방이 없습니다.");
			return;
		}
		
		for (ChatRoom room : targetRooms) {
			// 3. 대화 수 카운트
			long msgCount = chatMessageRepository.countByRoomId(room.getRoomId());
			
			// 4. 대화가 5번 미만이면 감점
			if (msgCount < 5) {
				log.info("📉 감점 대상 발견: 방ID={}, 대화수={}", room.getRoomId(), msgCount);
				penaltyMembers(room.getRoomId());
			} else {
				log.info("✅ 정상 활동 방: 방ID={}, 대화수={}", room.getRoomId(), msgCount);
			}
			
			// 5. 정산 완료 처리 (다시는 조회 안 됨)
			room.markAsEvaluated();
		}
	}
	
	// 감점 수행 메서드
	private void penaltyMembers(Long roomId) {
		// 방 멤버(2명) 조회
		List<ChatRoomMemberEntity> members = chatRoomMemberRepository.findByChatRoomId(roomId);
		
		for (ChatRoomMemberEntity member : members) {
			UserEntity user = member.getUser();
			
			// 🔥 여기서 점수 깎임! (Entity 메서드 호출)
			user.decreaseManner(0.1);
			
			log.info("   -> 유저[{}] 점수 차감 완료. ({} -> {})",
					user.getNickname(), user.getManner() + 0.1, user.getManner());
		}
	}
}