package com.scit48.community.repository;

import com.scit48.community.domain.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
	
	// 특정 게시글의 모든 댓글 조회 (오래된 순/최신 순)
	List<CommentEntity> findAllByBoard_BoardIdOrderByCreatedAtAsc(Long boardId);
	
	
}
