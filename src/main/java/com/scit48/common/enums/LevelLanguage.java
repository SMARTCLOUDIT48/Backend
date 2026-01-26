package com.scit48.common.enums;

public enum LevelLanguage {
	
	BEGINNER(1),
	INTERMEDIATE(2),
	ADVANCED(3),
	NATIVE(4);
	
	private final int score;
	
	LevelLanguage(int score) {
		this.score = score;
	}
	
	public int score() {
		return score;
	}
}