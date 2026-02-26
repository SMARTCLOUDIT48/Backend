package com.scit48.community.repository;

import com.scit48.admin.dto.AdminPostListDTO;
import com.scit48.community.domain.entity.BoardEntity;
import com.scit48.community.domain.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	Page<BoardEntity> findByUserNicknameContainingAndCategoryNameNot(String keyword, String excludeName,
			Pageable pageable);

	// '일상' 카테고리인 글만 조회 (피드용)
	Slice<BoardEntity> findByCategoryName(String categoryName, Pageable pageable);

	// 마이페이지 - 내 게시글 전용 쿼리 (일상 카테고리 제외)
	// 1. 카테고리/검색어 없음 (전체 기본)
	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.category.name != '일상'")
	Page<BoardEntity> findMyBoardAll(@Param("memberId") String memberId, Pageable pageable);

	// 2. 카테고리만 선택됨
	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.category.name = :cateName")
	Page<BoardEntity> findMyBoardByCategory(@Param("memberId") String memberId, @Param("cateName") String cateName,
			Pageable pageable);

	// 3. 검색어만 있음 (전체 중 검색)
	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.category.name != '일상' AND b.title LIKE %:keyword%")
	Page<BoardEntity> findMyBoardByTitle(@Param("memberId") String memberId, @Param("keyword") String keyword,
			Pageable pageable);

	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.category.name != '일상' AND b.content LIKE %:keyword%")
	Page<BoardEntity> findMyBoardByContent(@Param("memberId") String memberId, @Param("keyword") String keyword,
			Pageable pageable);

	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.category.name != '일상' AND (b.title LIKE %:keyword% OR b.content LIKE %:keyword%)")
	Page<BoardEntity> findMyBoardByBoth(@Param("memberId") String memberId, @Param("keyword") String keyword,
			Pageable pageable);

	// 4. 카테고리 + 검색어 동시 적용
	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.category.name = :cateName AND b.title LIKE %:keyword%")
	Page<BoardEntity> findMyBoardByCategoryAndTitle(@Param("memberId") String memberId,
			@Param("cateName") String cateName, @Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.category.name = :cateName AND b.content LIKE %:keyword%")
	Page<BoardEntity> findMyBoardByCategoryAndContent(@Param("memberId") String memberId,
			@Param("cateName") String cateName, @Param("keyword") String keyword, Pageable pageable);

	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.category.name = :cateName AND (b.title LIKE %:keyword% OR b.content LIKE %:keyword%)")
	Page<BoardEntity> findMyBoardByCategoryAndBoth(@Param("memberId") String memberId,
			@Param("cateName") String cateName, @Param("keyword") String keyword, Pageable pageable);

	// userPage 게시판 목록 출력용
	// 1. 검색어가 없을 때 (일상피드 제외)
	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.title != '일상 피드입니다.'")
	Page<BoardEntity> findAllByMemberId(@Param("memberId") String memberId, Pageable pageable);

	// 2. 제목으로 검색 (일상피드 제외)
	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.title != '일상 피드입니다.' AND b.title LIKE %:keyword%")
	Page<BoardEntity> findByMemberIdAndTitle(@Param("memberId") String memberId, @Param("keyword") String keyword,
			Pageable pageable);

	// 3. 내용으로 검색 (일상피드 제외)
	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.title != '일상 피드입니다.' AND b.content LIKE %:keyword%")
	Page<BoardEntity> findByMemberIdAndContent(@Param("memberId") String memberId, @Param("keyword") String keyword,
			Pageable pageable);

	// 4. 제목 + 내용으로 검색 (일상피드 제외)
	@Query("SELECT b FROM BoardEntity b WHERE b.user.memberId = :memberId AND b.title != '일상 피드입니다.' AND (b.title LIKE %:keyword% OR b.content LIKE %:keyword%)")
	Page<BoardEntity> findByMemberIdAndTitleOrContent(@Param("memberId") String memberId,
			@Param("keyword") String keyword, Pageable pageable);

	// 관리자 페이지에서 불러오기 용
	@Query(value = """
			    SELECT
			        DATE(created_at) AS label,
			        COUNT(*) AS value
			    FROM board
			    GROUP BY DATE(created_at)
			    ORDER BY DATE(created_at)
			""", nativeQuery = true)
	List<Object[]> countDailyStats();

	@Query(value = """
			    SELECT
			        YEARWEEK(created_at, 1) AS label,
			        COUNT(*) AS value
			    FROM board
			    GROUP BY YEARWEEK(created_at, 1)
			    ORDER BY YEARWEEK(created_at, 1)
			""", nativeQuery = true)
	List<Object[]> countWeeklyStats();

	@Query(value = """
			    SELECT
			        DATE_FORMAT(created_at, '%Y-%m') AS label,
			        COUNT(*) AS value
			    FROM board
			    GROUP BY DATE_FORMAT(created_at, '%Y-%m')
			    ORDER BY DATE_FORMAT(created_at, '%Y-%m')
			""", nativeQuery = true)
	List<Object[]> countMonthlyStats();

	// 조회수 증가 (update 쿼리)
	@Modifying
	@Query("update BoardEntity b set b.viewCount = b.viewCount + 1 where b.boardId = :boardId")
	void updateHits(@Param("boardId") Long boardId);

	// 관리자 페이지에서 불러오기 용
	@Query("""
			    SELECT new com.scit48.admin.dto.AdminPostListDTO(
			        'COMMUNITY',
			        b.boardId,
			        b.title,
			        u.nickname,
			        b.createdAt,
			        CONCAT('/community/detail/', b.boardId)
			    )
			    FROM BoardEntity b
			    JOIN b.user u
			    WHERE
			      (
			        :keyword IS NULL OR :keyword = ''
			        OR (
			          :searchType = 'TITLE' AND b.title LIKE %:keyword%
			        )
			        OR (
			          :searchType = 'AUTHOR' AND u.nickname LIKE %:keyword%
			        )
			      )
			    ORDER BY b.createdAt DESC
			""")
	Page<AdminPostListDTO> findAdminBoardPosts(
			@Param("searchType") String searchType, // TITLE / AUTHOR
			@Param("keyword") String keyword,
			Pageable pageable);

	long countByUser_MemberId(String memberId);
}