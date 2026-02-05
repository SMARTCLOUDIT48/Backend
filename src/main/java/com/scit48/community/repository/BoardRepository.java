package com.scit48.community.repository;

import com.scit48.community.domain.entity.BoardEntity;
import com.scit48.community.domain.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BoardRepository
			extends JpaRepository<BoardEntity, Long> {
	// 0. 카테고리별 게시글 목록 조회 (페이징 포함)
	Page<BoardEntity> findAllByCategory(CategoryEntity category, Pageable pageable);
	
	// 1. 기본 전체 목록 (일상 제외)
	Page<BoardEntity> findByCategoryNameNot(String excludeName, Pageable pageable);
	
	// 2. 카테고리 필터링 (일상 제외 + 특정 카테고리 선택)
	Page<BoardEntity> findByCategoryNameAndCategoryNameNot(String categoryName, String excludeName, Pageable pageable);
	
	// 3. 검색 기능 (제목, 내용, 작성자) - 일상 제외 조건 포함
	// 예: 제목 검색
	Page<BoardEntity> findByTitleContainingAndCategoryNameNot(String keyword, String excludeName, Pageable pageable);
	
	// 예: 내용 검색
	Page<BoardEntity> findByContentContainingAndCategoryNameNot(String keyword, String excludeName, Pageable pageable);
	
	// 예: 작성자 검색
	Page<BoardEntity> findByUserNicknameContainingAndCategoryNameNot(String keyword, String excludeName, Pageable pageable);
	
	
	// '일상' 카테고리인 글만 조회 (피드용)
	Page<BoardEntity> findByCategoryName(String categoryName, Pageable pageable);
	
	
	/*
	 * 일상공유 피드용 전체 조회 (SNS 스타일)
	 * @EntityGraph를 사용하여 연관된 Member 정보를 한 번의 쿼리로 가져옵니다.
	 * 이를 통해 피드 목록에서 작성자의 닉네임, 프로필 이미지를 효율적으로 표시할 수 있습니다.
	@Override
	@EntityGraph(attributePaths = {"member", "category"})
	Page<BoardEntity> findAll(Pageable pageable);
	*/
	
	
	@Query(
			value = """
    SELECT
      DATE(created_at) AS label,
      COUNT(*) AS value
    FROM board
    WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY)
    GROUP BY DATE(created_at)
    ORDER BY DATE(created_at)
  """,
			nativeQuery = true
	)
	List<Object[]> countStatsDaily();
	
	
	// 조회수 증가 (update 쿼리)
	@Modifying
	@Query("update BoardEntity b set b.viewCount = b.viewCount + 1 where b.boardId = :boardId")
	void updateHits(@Param("boardId") Long boardId);
	
	
}
