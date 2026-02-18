package com.scit48.Inquiry.repository;


import com.scit48.Inquiry.domain.entity.InquiryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
	
	List<InquiryEntity> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
	
	Optional<InquiryEntity> findByInquiryIdAndUserIdAndIsActiveTrue(
			Long inquiryId,
			Long userId
	);
	
	@Query(
			value = """
    SELECT
      DATE(created_at) AS label,
      COUNT(*) AS value
    FROM inquiry
    WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY)
    GROUP BY DATE(created_at)
    ORDER BY DATE(created_at)
  """,
			nativeQuery = true
	)
	List<Object[]> countStatsDaily();
	
	Page<InquiryEntity> findAllByIsActiveTrue(Pageable pageable);
	
	@Query("""
    SELECT i FROM InquiryEntity i
    JOIN UserEntity u ON i.userId = u.id
    WHERE i.isActive = true
      AND u.nickname LIKE %:keyword%
""")
	Page<InquiryEntity> searchByUserNickname(
			@Param("keyword") String keyword,
			Pageable pageable
	);
	
	@Query("""
    SELECT i FROM InquiryEntity i
    WHERE i.isActive = true
      AND i.title LIKE %:keyword%
""")
	Page<InquiryEntity> searchByTitle(
			@Param("keyword") String keyword,
			Pageable pageable
	);
	
	@Query("""
    SELECT i FROM InquiryEntity i
    JOIN UserEntity u ON i.userId = u.id
    WHERE i.isActive = true
      AND (u.nickname LIKE %:keyword%
           OR i.title LIKE %:keyword%)
""")
	Page<InquiryEntity> searchByUserNicknameOrTitle(
			@Param("keyword") String keyword,
			Pageable pageable
	);
	
}