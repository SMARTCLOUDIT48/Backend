package com.scit48.Inquiry.domain.entity;

public enum InquiryType {
	
	ACCOUNT("계정"),
	CHAT("채팅"),
	REPORT("신고"),
	ETC("기타");
	
	private final String label;
	
	InquiryType(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
