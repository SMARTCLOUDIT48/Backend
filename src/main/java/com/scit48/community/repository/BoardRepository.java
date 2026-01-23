package com.scit48.community.repository;

import com.scit48.community.domain.entity.BoardEntity;
import com.scit48.community.domain.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository
			extends JpaRepository<BoardEntity, Long> {
	// 1. 카테고리별 게시글 목록 조회 (페이징 포함)
	Page<BoardEntity> findAllByCategory(CategoryEntity category, Pageable pageable);
	
	// 2. 제목 또는 내용으로 검색 (필요 시 추가)
	Page<BoardEntity> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
	
	// 3. 일상공유 피드용 전체 조회 (작성일 기준 내림차순은 Pageable에서 처리)
	/**
	 * 3. 일상공유 피드용 전체 조회 (SNS 스타일)
	 * @EntityGraph를 사용하여 연관된 Member 정보를 한 번의 쿼리로 가져옵니다.
	 * 이를 통해 피드 목록에서 작성자의 닉네임, 프로필 이미지를 효율적으로 표시할 수 있습니다.
	 */
	@Override
	@EntityGraph(attributePaths = {"member", "category"})
	Page<BoardEntity> findAll(Pageable pageable);
}
