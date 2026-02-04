package com.scit48.chat.repository;

import com.scit48.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	// 기본 기능(저장, 조회)은 JpaRepository가 다 해줍니다!
	boolean existsByName(String name);
	
	//중복방 생성 방지 기능
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
}