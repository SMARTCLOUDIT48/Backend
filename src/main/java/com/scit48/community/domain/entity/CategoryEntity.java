package com.scit48.community.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category")
@Builder
public class CategoryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Long id;
	
	@Column(nullable = false, unique = true)
	private String name; // 예: "Q&A", "RECRUIT" 등
	
	public CategoryEntity(String name) {
		this.name = name;
	}
}
