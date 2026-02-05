package com.scit48.Inquiry.repository;


import com.scit48.Inquiry.domain.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
	
	
}