package com.scit48.community.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeKey {
	
	
	@Column(name = "board_id")
	private Long boardId;
	
	@Column(name = "ip", length = 50)
	private String ip;
	
}
