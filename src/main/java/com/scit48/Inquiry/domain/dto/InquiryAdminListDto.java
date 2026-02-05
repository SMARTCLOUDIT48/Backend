package com.scit48.Inquiry.domain.dto;

import com.scit48.Inquiry.domain.entity.InquiryStatus;
import com.scit48.Inquiry.domain.entity.InquiryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryAdminListDto {
	private Long inquiryId;
	private String userNickname;
	private InquiryType type;
	private String title;
	private String content;
	private String answerContent;
	private InquiryStatus status;
	private LocalDateTime createdAt;
	private String attachmentPath;
}
