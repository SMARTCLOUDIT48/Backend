package com.scit48.chat.repository;

import com.scit48.chat.domain.ChatRoomMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMemberEntity, Long> {
	
	// ✅ 기존: 방 안의 멤버들(상대 찾기용)
	@Query("SELECT m FROM ChatRoomMemberEntity m JOIN FETCH m.user WHERE m.room.roomId = :roomId")
	List<ChatRoomMemberEntity> findByChatRoomId(@Param("roomId") Long roomId);
	
	// ✅ [추가 1] 내가 속한 멤버십 목록 (방 목록/lastReadMsgId 용)
	@Query("SELECT m FROM ChatRoomMemberEntity m JOIN FETCH m.room WHERE m.user.id = :userId")
	List<ChatRoomMemberEntity> findMyMemberships(@Param("userId") Long userId);
	
	// ✅ [수정] 에러 해결 버전 (한 줄 쿼리로 변경)
	@Query("SELECT m FROM ChatRoomMemberEntity m JOIN FETCH m.room JOIN FETCH m.user WHERE m.user.id = :userId AND m.room.roomId = :roomId")
	Optional<ChatRoomMemberEntity> findMyMembership(@Param("userId") Long userId, @Param("roomId") Long roomId);
}
