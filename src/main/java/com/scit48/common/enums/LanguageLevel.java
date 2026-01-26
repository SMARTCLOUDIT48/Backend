package com.scit48.common.enums;

public enum LanguageLevel {
	BEGINNER(1),
	INTERMEDIATE(2),
	ADVANCED(3),
	NATIVE(4);
	
	private final int score;
	
	LanguageLevel(int score) {
		this.score = score;
	}
	
	public int score() {
		return score;
	}
}
