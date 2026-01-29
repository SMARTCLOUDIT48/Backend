package com.scit48.notice.service;

import com.scit48.notice.domain.dto.NoticeDTO;
import com.scit48.notice.domain.entity.NoticeEntity;
import com.scit48.notice.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {
	private final NoticeRepository noticeRepository;
	

	
	public Page<NoticeDTO> getNoticePage(int page) {
		Pageable pageable = PageRequest.of(page, 10); // 한 페이지 5개
		
		return noticeRepository
				.findByTypeAndIsActiveOrderByIsPinnedDescCreatedAtDesc(
						"NOTICE",
						true,
						pageable
				)
				.map(NoticeDTO::from);
	}
	
	public Page<NoticeDTO> getFaqPage(int page) {
		Pageable pageable = PageRequest.of(page, 10);
		
		return noticeRepository
				.findByTypeAndIsActiveOrderBySortOrderAsc(
						"FAQ",
						true,
						pageable
				)
				.map(NoticeDTO::from);
	}
	
	public Page<NoticeDTO> search(String type, String keyword, int page) {
		PageRequest pageable = PageRequest.of(
				page, 10,
				Sort.by("isPinned").descending()
						.and(Sort.by("createdAt").descending())
		);
		
		return noticeRepository
				.findByTypeAndIsActiveAndTitleContainingIgnoreCase(type, true, keyword.trim(), pageable)
				.map(NoticeDTO::from);
	}
	
	public NoticeDTO getNotice(Long id) {
		return noticeRepository.findById(id)
				.filter(NoticeEntity::isActive)
				.map(NoticeDTO::from)
				.orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
	}
	
	@Transactional
	public void updateNotice(Long id, String title, String content) {
		NoticeEntity notice = noticeRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
		
		notice.update(title, content);
	}
	
	@Transactional
	public void deleteNotice(Long id) {
		NoticeEntity notice = noticeRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
		
		notice.setActive(false); // 실제 삭제 ❌ (소프트 삭제)
	}
	
	
}
