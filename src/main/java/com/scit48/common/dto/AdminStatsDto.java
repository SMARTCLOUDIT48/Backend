package com.scit48.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStatsDto {
	
	private String label;   // 날짜 문자열
	private Long value;     // count
}
