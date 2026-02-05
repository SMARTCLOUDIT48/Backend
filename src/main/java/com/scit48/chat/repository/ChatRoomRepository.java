package com.scit48.chat.repository;

import com.scit48.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List; // List import 필수
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	
	// 1. 방 이름 중복 체크 (기존)
	boolean existsByName(String name);
	
	// 2. 중복 방 생성 방지 (기존)
	@Query("""
        select m1.room.roomId
        from ChatRoomMemberEntity m1
        join ChatRoomMemberEntity m2
          on m1.room.roomId = m2.room.roomId
        where m1.user.id = :userA
          and m2.user.id = :userB
    """)
	Optional<Long> findSharedRoomId(@Param("userA") Long userA,
									@Param("userB") Long userB);
	
	// ✅ [추가] 중요! 내(userId)가 참여 중인 방 목록만 가져오기
	// 설명: ChatRoomMemberEntity 테이블에서 내 아이디(userId)가 들어있는 방(m.room)만 쏙 뽑아옵니다.
	@Query("SELECT m.room FROM ChatRoomMemberEntity m WHERE m.user.id = :userId")
	List<ChatRoom> findMyChatRooms(@Param("userId") Long userId);
}