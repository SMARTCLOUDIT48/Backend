package com.scit48.notice.repository;

import com.scit48.admin.dto.AdminPostListDTO;
import com.scit48.notice.domain.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
	// 공지사항 (페이지네이션)
	Page<NoticeEntity> findByTypeAndIsActiveOrderByIsPinnedDescCreatedAtDesc(
			String type,
			boolean isActive,
			Pageable pageable
	);
	
	// FAQ (페이지네이션)
	Page<NoticeEntity> findByTypeAndIsActiveOrderBySortOrderAsc(
			String type,
			boolean isActive,
			Pageable pageable
	);
	
	// ✅ 타입 + 제목 검색 (대소문자 무시)
	Page<NoticeEntity> findByTypeAndIsActiveAndTitleContainingIgnoreCase(
			String type,
			boolean isActive,
			String keyword,
			Pageable pageable
	);
	
	@Query(value = """
    SELECT
        DATE(created_at) AS label,
        COUNT(*) AS value
    FROM notice
    GROUP BY DATE(created_at)
    ORDER BY DATE(created_at)
""", nativeQuery = true)
	List<Object[]> countDailyStats();
	
	
	@Query(value = """
    SELECT
        YEARWEEK(created_at, 1) AS label,
        COUNT(*) AS value
    FROM notice
    GROUP BY YEARWEEK(created_at, 1)
    ORDER BY YEARWEEK(created_at, 1)
""", nativeQuery = true)
	List<Object[]> countWeeklyStats();
	
	
	@Query(value = """
    SELECT
        DATE_FORMAT(created_at, '%Y-%m') AS label,
        COUNT(*) AS value
    FROM notice
    GROUP BY DATE_FORMAT(created_at, '%Y-%m')
    ORDER BY DATE_FORMAT(created_at, '%Y-%m')
""", nativeQuery = true)
	List<Object[]> countMonthlyStats();
	
	
	@Query("""
        SELECT new com.scit48.admin.dto.AdminPostListDTO(
            n.type,
            n.noticeId,
            n.title,
            u.nickname,
            n.createdAt,
            CONCAT('/customer/notice/', n.noticeId)
        )
        FROM NoticeEntity n
        JOIN UserEntity u ON u.id = n.createdBy
        WHERE
          (:board IS NULL OR n.type = :board)
          AND (
            :keyword IS NULL OR :keyword = ''
            OR (
              :searchType = 'TITLE' AND n.title LIKE %:keyword%
            )
            OR (
              :searchType = 'AUTHOR' AND u.nickname LIKE %:keyword%
            )
          )
        ORDER BY n.createdAt DESC
    """)
	Page<AdminPostListDTO> findAdminNoticePosts(
			@Param("board") String board,            // NOTICE / FAQ
			@Param("searchType") String searchType,  // TITLE / AUTHOR
			@Param("keyword") String keyword,
			Pageable pageable
	);
}
