package com.scit48.Inquiry.service;

import com.scit48.Inquiry.domain.entity.*;
import com.scit48.Inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {
	
	private final InquiryRepository inquiryRepository;
	
	public void create(
			Long userId,
			InquiryType type,
			String title,
			String content,
			String attachmentName,
			String attachmentPath
	) {
		InquiryEntity inquiry = InquiryEntity.builder()
				.userId(userId)
				.type(type)
				.title(title)
				.content(content)
				.attachmentName(attachmentName)
				.attachmentPath(attachmentPath)
				.status(InquiryStatus.WAITING)
				.isActive(true)
				.build();
		
		inquiryRepository.save(inquiry);
	}
	
	@Transactional(readOnly = true)
	public List<InquiryEntity> findMyInquiries(Long userId) {
		return inquiryRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
	}
	
	public InquiryEntity findMyInquiry(Long id, Long userId) {
		return inquiryRepository
				.findByInquiryIdAndUserIdAndIsActiveTrue(id, userId)
				.orElseThrow(() -> new RuntimeException("문의 없음"));
	}
	
	public void update(
			Long id,
			Long userId,
			String title,
			String content,
			String attachmentName,
			String attachmentPath
	) {
		InquiryEntity inquiry = findMyInquiry(id, userId);
		
		inquiry.setTitle(title);
		inquiry.setContent(content);
		
		// 이미지가 새로 들어온 경우만 교체
		if (attachmentPath != null) {
			inquiry.setAttachmentName(attachmentName);
			inquiry.setAttachmentPath(attachmentPath);
		}
	}

	
	public void delete(Long id, Long userId) {
		InquiryEntity inquiry = findMyInquiry(id, userId);
		inquiryRepository.delete(inquiry);
	}
	
	
}
