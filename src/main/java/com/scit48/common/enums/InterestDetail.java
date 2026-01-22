package com.scit48.common.enums;

public enum InterestDetail {
	
	/*detail 항목 추가 필요*/
	BACKEND("BACKEND"),
	FRONTEND("FRONTEND"),
	MOBILE("MOBILE"),
	
	JAPAN("JAPAN"),
	EUROPE("EUROPE"),
	
	JAPANESE("JAPANESE"),
	ENGLISH("ENGLISH");
	
	private final String value;
	
	InterestDetail(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}