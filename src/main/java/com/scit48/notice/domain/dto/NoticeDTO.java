package com.scit48.notice.domain.dto;

import com.scit48.notice.domain.entity.NoticeEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeDTO {
	private Long noticeId;
	private String title;
	private String category;
	private String type;
	private String content;
	private boolean isPinned;
	private LocalDateTime createdAt;
	
	public static NoticeDTO from(NoticeEntity notice) {
		return NoticeDTO.builder()
				.noticeId(notice.getNoticeId())
				.title(notice.getTitle())
				.category(notice.getCategory())
				.type(notice.getType())
				.content(notice.getContent())
				.isPinned(notice.isPinned())
				.createdAt(notice.getCreatedAt())
				.build();
	}
}

