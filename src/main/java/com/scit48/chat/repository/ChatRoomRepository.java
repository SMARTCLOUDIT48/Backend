package com.scit48.chat.repository;

import com.scit48.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	// 기본 기능(저장, 조회)은 JpaRepository가 다 해줍니다!
	boolean existsByName(String name);
}