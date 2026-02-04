package com.scit48.Inquiry.service;


import com.scit48.Inquiry.domain.dto.InquiryAdminListDto;
import com.scit48.Inquiry.domain.entity.InquiryAnswerEntity;
import com.scit48.Inquiry.domain.entity.InquiryEntity;
import com.scit48.Inquiry.domain.entity.InquiryStatus;
import com.scit48.Inquiry.repository.InquiryAnswerRepository;
import com.scit48.Inquiry.repository.InquiryRepository;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminInquiryService {
	private final InquiryRepository inquiryRepository;
	private final InquiryAnswerRepository inquiryAnswerRepository;
	private final UserRepository userRepository;
	
	/**
	 * 관리자 문의 목록
	 */
	@Transactional(readOnly = true)
	public List<InquiryAdminListDto> getInquiryList() {
		return inquiryRepository.findAll().stream()
				.filter(InquiryEntity::isActive)
				.map(inquiry -> {
					
					UserEntity user = userRepository
							.findById(inquiry.getUserId())
							.orElse(null);
					
					// ⭐ 답변 내용 조회 (있으면, 없으면 null)
					String answerContent = inquiryAnswerRepository
							.findByInquiry_InquiryId(inquiry.getInquiryId())
							.map(InquiryAnswerEntity::getContent)
							.orElse(null);
					
					return InquiryAdminListDto.builder()
							.inquiryId(inquiry.getInquiryId())
							.userNickname(user != null ? user.getNickname() : "탈퇴회원")
							.type(inquiry.getType())
							.title(inquiry.getTitle())
							.content(inquiry.getContent())          // 문의 내용
							.answerContent(answerContent)           // ⭐ 답변 내용
							.status(inquiry.getStatus())
							.createdAt(inquiry.getCreatedAt())
							.attachmentPath(inquiry.getAttachmentPath())
							.build();
				})
				.toList();
	}
	
	
	/**
	 * 관리자 답변 등록
	 */
	public void answerInquiry(Long inquiryId, Long adminId, String content) {
		
		InquiryEntity inquiry = inquiryRepository.findById(inquiryId)
				.orElseThrow(() -> new IllegalArgumentException("문의 없음"));
		
		if (inquiryAnswerRepository.existsByInquiry_InquiryId(inquiryId)) {
			throw new IllegalStateException("이미 답변된 문의입니다.");
		}
		
		UserEntity admin = userRepository.findById(adminId)
				.orElseThrow(() -> new IllegalArgumentException("관리자 없음"));
		
		InquiryAnswerEntity answer = InquiryAnswerEntity.builder()
				.inquiry(inquiry)
				.admin(admin)
				.content(content)
				.build();
		
		// ✅ 1️⃣ inquiry_answer 테이블 저장
		inquiryAnswerRepository.save(answer);
		
		// ✅ 2️⃣ inquiry 상태 변경
		inquiry.setStatus(InquiryStatus.ANSWERED);
		
		// ✅ 3️⃣ inquiry 테이블에 반영 (핵심)
		inquiryRepository.save(inquiry);
	}
	
	public void updateAnswer(Long inquiryId, Long adminId, String content) {
		
		InquiryAnswerEntity answer = inquiryAnswerRepository
				.findByInquiry_InquiryId(inquiryId)
				.orElseThrow(() -> new IllegalArgumentException("답변 없음"));
		
		answer.updateContent(content);
	}
	
}
