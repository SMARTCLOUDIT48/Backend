package com.scit48.community.repository;

import com.scit48.community.domain.entity.LikeEntity;
import com.scit48.community.domain.entity.LikeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, LikeKey> {
	// 특정 회원이 특정 게시글에 누른 좋아요 정보 찾기
	Optional<LikeEntity> findByUser_IdAndBoard_BoardId(Long memberId, Long boardId);
	
	// 좋아요 존재 여부 확인 (토글 기능용)
	boolean existsByUser_IdAndBoard_BoardId(Long memberId, Long boardId);
	
	// 좋아요 취소 시 삭제
	void deleteByUser_IdAndBoard_BoardId(Long memberId, Long boardId);
	
}
