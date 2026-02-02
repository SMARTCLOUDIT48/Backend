package com.scit48.notice.repository;

import com.scit48.notice.domain.entity.NoticeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
