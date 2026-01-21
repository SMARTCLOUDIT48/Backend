package com.scit48.common.enums;

public enum InterestType {
	
	/*추가 필요*/
	IT("IT"),
	TRAVEL("TRAVEL"),
	STUDY("STUDY"),
	HEALTH("HEALTH"),
	LANGUAGE("LANGUAGE");
	
	private final String value;
	
	InterestType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}