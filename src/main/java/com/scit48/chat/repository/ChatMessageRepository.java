package com.scit48.chat.repository;

import com.scit48.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	// 나중에 "방 별로 메시지 가져오기" 같은 기능이 필요하면 여기에 추가합니다.
	// List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);
}