package com.scit48.chat.repository;

import com.scit48.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
	
	// 수정 전: findByRoomIdOrderByIdAsc
	// 수정 후: findByRoomIdOrderByMsgIdAsc (Id -> MsgId)
	List<ChatMessage> findByRoomIdOrderByMsgIdAsc(Long roomId);
	// ✅ 특정 방의 메시지 총 개수 세기
	long countByRoomId(Long roomId);
}