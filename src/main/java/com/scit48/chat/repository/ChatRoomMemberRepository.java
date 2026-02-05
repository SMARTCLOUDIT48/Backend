package com.scit48.chat.repository;

import com.scit48.chat.domain.ChatRoomMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMemberEntity, Long> {
	
	// ✅ 변경점: User 정보를 확실하게 같이 가져오도록 'JOIN FETCH' 사용
	// 이렇게 하면 프로필 사진, 닉네임 등을 가져올 때 추가 쿼리가 발생하지 않아 빠르고 안전합니다.
	@Query("SELECT m FROM ChatRoomMemberEntity m JOIN FETCH m.user WHERE m.room.roomId = :roomId")
	List<ChatRoomMemberEntity> findByChatRoomId(@Param("roomId") Long roomId);
}