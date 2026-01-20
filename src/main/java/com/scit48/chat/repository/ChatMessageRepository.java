package com.scit48.chat.repository;

import com.scit48.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	
	
	List<ChatMessage> findByRoomIdOrderByIdAsc(Long roomId);
}