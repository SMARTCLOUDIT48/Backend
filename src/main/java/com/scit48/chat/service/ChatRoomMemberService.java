package com.scit48.chat.service;

import com.scit48.chat.domain.ChatRoom;
import com.scit48.chat.domain.ChatRoomMemberEntity;
import com.scit48.chat.domain.dto.DirectRoomResponseDTO;
import com.scit48.chat.repository.ChatRoomMemberRepository;
import com.scit48.chat.repository.ChatRoomRepository;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ChatRoomMemberService {
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final UserRepository userRepository;
	
	public DirectRoomResponseDTO createOrGetDirectRoom(Long myId, Long partnerId) {
		if(myId.equals(partnerId)){
			throw new IllegalArgumentException("본인에게는 채팅 신청을 할 수 없습니다.");
		}
		
		//1) 이미 둘이 공유하는 방이 있으면 재사용
		var sharedRoomIdOpt = chatRoomRepository.findSharedRoomId(myId,partnerId);
		if(sharedRoomIdOpt.isPresent()){
			Long roomId =sharedRoomIdOpt.get();
			ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(
					() -> new EntityNotFoundException("채팅방이 존재하지 않습니다: " + roomId));
			return DirectRoomResponseDTO.of(room.getRoomId(), room.getRoomUuid(), partnerId);
		}
		
		// 2) 유저 존재 확인
		UserEntity me = userRepository.findById(myId)
				.orElseThrow(() -> new IllegalArgumentException("회원 없음: " + myId));
		UserEntity partner = userRepository.findById(partnerId)
				.orElseThrow(() -> new IllegalArgumentException("상대 회원 없음: " + partnerId));
		
		// 3) 채팅방 생성 (ChatRoom 엔티티가 name을 필수로 가진 구조라면 name 필요)
		// 방이름 임의 생성 닉네임 & 닉네임
		String roomName = me.getNickname() + " & " + partner.getNickname();
		ChatRoom room = chatRoomRepository.save(new ChatRoom(roomName));
		
		// 4) 멤버 2명 insert
		chatRoomMemberRepository.save(ChatRoomMemberEntity.builder()
				.room(room)
				.user(me)
				.roomName(null)
				.build());
		
		chatRoomMemberRepository.save(ChatRoomMemberEntity.builder()
				.room(room)
				.user(partner)
				.roomName(null)
				.build());
		
		return DirectRoomResponseDTO.of(room.getRoomId(), room.getRoomUuid(), partnerId);
	}
}
