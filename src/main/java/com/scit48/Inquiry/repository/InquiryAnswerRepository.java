package com.scit48.Inquiry.repository;

import com.scit48.Inquiry.domain.entity.InquiryAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswerEntity, Long> {
	Optional<InquiryAnswerEntity> findByInquiry_InquiryId(Long inquiryId);
	
	boolean existsByInquiry_InquiryId(Long inquiryId);
}
