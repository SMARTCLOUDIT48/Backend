package com.scit48.recommend.service;

import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.domain.ChatRoomMemberEntity;
import com.scit48.chat.repository.ChatRoomMemberRepository;
import com.scit48.chat.repository.ChatRoomRepository;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.enums.Gender;
import com.scit48.common.repository.UserRepository;
import com.scit48.recommend.domain.dto.MatchResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MatchService {
	private final UserRepository userRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final RedisMatchQueueService redisMatchQueueService;
	
	private final RedisTemplate<String, Object> redisObjectTemplate;
	
	private static final long RESULT_TTL_MIN = 10;
	
	@Transactional
	public MatchResponseDTO start(Long myId, String criteriaKey) {
		// 0) 혹시 이미 매칭 결과가 있으면 그걸 우선 반환(중복 클릭 방지)
		MatchResponseDTO cached = getResult(myId);
		if (cached != null && "MATCHED".equals(cached.getStatus())) {
			return cached;
		}
		
		// 1) criteriaKey 생성 (현재는 성별/국가 반전만 예시)
		UserEntity me = userRepository.findById(myId)
				.orElseThrow(() -> new IllegalArgumentException("유저 없음: " + myId));
		
//		// 필터링 부분 일단 js에서 필터링 키를 만들어서 보내기
//		Gender targetGender = (me.getGender() == Gender.MALE) ? Gender.FEMALE : Gender.MALE;
//		String targetNation = me.getNation().equals("KOREA") ? "JAPAN" : "KOREA";
//
//		String criteriaKey = "g=" + targetGender + "|n=" + targetNation;
		
		// 2) Redis 큐에서 매칭 시도
		Long partnerId = redisMatchQueueService.tryMatch(criteriaKey, myId);
		
		// 3) 매칭 실패 -> WAITING
		if (partnerId == null) {
			return MatchResponseDTO.waiting();
		}
		
		// 4) 매칭 성공 -> DB에 방 생성 + 멤버 2명 insert
		UserEntity partner = userRepository.findById(partnerId)
				.orElseThrow(() -> new IllegalArgumentException("파트너 유저 없음: " + partnerId));
		
		String roomName = "direct:" + Math.min(myId, partnerId) + ":" + Math.max(myId, partnerId);
		ChatRoom room = chatRoomRepository.save(new ChatRoom(roomName));
		
		chatRoomMemberRepository.save(ChatRoomMemberEntity.builder()
				.room(room).user(me).roomName(null).build());
		
		chatRoomMemberRepository.save(ChatRoomMemberEntity.builder()
				.room(room).user(partner).roomName(null).build());
		
		// 5) Redis에 “결과” 저장 (폴링이 이걸 읽음)
		MatchResponseDTO myRes = MatchResponseDTO.matched(room.getRoomId(), room.getRoomUuid(), partnerId);
		MatchResponseDTO partnerRes = MatchResponseDTO.matched(room.getRoomId(), room.getRoomUuid(), myId);
		
		setResult(myId, myRes);
		setResult(partnerId, partnerRes);
		
		return myRes;
	}
	
	public MatchResponseDTO getOrWaiting(Long myId) {
		MatchResponseDTO res = getResult(myId);
		return (res != null) ? res : MatchResponseDTO.waiting();
	}
	
	private void setResult(Long userId, MatchResponseDTO res) {
		String key = "match:result:" + userId;
		redisObjectTemplate.opsForValue().set(key, res, RESULT_TTL_MIN, TimeUnit.MINUTES);
	}
	
	private MatchResponseDTO getResult(Long userId) {
		String key = "match:result:" + userId;
		Object obj = redisObjectTemplate.opsForValue().get(key);
		if (obj instanceof MatchResponseDTO mr) return mr;
		return null;
	}
}
