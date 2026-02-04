package com.scit48.chat.repository;

import com.scit48.chat.domain.ChatRoomMemberEntity;
import com.scit48.common.domain.entity.UserEntity; // UserEntity import 확인
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMemberEntity, Long> {
	
	// ✅ [추가] 특정 방(roomId)에서 나(myId) 말고 다른 유저(상대방) 찾기
	@Query("SELECT m.user FROM ChatRoomMemberEntity m WHERE m.room.roomId = :roomId AND m.user.id != :myId")
	Optional<UserEntity> findOpponent(@Param("roomId") Long roomId, @Param("myId") Long myId);
}