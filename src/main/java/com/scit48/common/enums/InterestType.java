package com.scit48.common.enums;

public enum InterestType {
	
	CULTURE("문화·예술"),
	HOBBY("취미·여가"),
	SPORTS("운동·스포츠"),
	TRAVEL("여행·지역"),
	FOOD("음식·요리"),
	STUDY("학습·자기계발"),
	IT("IT·기술"),
	LIFESTYLE("라이프스타일");
	
	private final String value;
	
	InterestType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
