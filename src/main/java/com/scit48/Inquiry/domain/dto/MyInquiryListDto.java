package com.scit48.Inquiry.domain.dto;

import com.scit48.Inquiry.domain.entity.InquiryStatus;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MyInquiryListDto {
	private Long inquiryId;
	private String title;
	private String content;
	private InquiryStatus status;
	private String answerContent; // ⭐ 관리자 답변
}
