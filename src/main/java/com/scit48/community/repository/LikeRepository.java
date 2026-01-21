package com.scit48.community.repository;

import com.scit48.community.domain.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
	// 특정 회원이 특정 게시글에 누른 좋아요 정보 찾기
	Optional<LikeEntity> findByMemberIdAndBoardId(Long memberId, Long boardId);
	
	// 좋아요 존재 여부 확인 (토글 기능용)
	boolean existsByMemberIdAndBoardId(Long memberId, Long boardId);
	
	// 좋아요 취소 시 삭제
	void deleteByMemberIdAndBoardId(Long memberId, Long boardId);
	
}
