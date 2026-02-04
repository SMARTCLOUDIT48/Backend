package com.scit48.Inquiry.repository;


import com.scit48.Inquiry.domain.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
	
	List<InquiryEntity> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
	
	Optional<InquiryEntity> findByInquiryIdAndUserIdAndIsActiveTrue(
			Long inquiryId,
			Long userId
	);
	
}