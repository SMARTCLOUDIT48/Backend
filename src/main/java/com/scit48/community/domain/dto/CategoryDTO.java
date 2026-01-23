package com.scit48.community.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
	private Long id;   // 조회 시 사용
	private String name; // 생성 및 조회 시 사용
}
