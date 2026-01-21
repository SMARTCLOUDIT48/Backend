package com.scit48.community.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "board")
@EntityListeners(AuditingEntityListener.class)
public class BoardEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "board_id")
	private Long id;
	
	@Column(nullable = false)
	private String title;
	
	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;
	
	private int viewCount;
	
	private String fileName;
	private String filePath;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private UserEntity user; // 작성자
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private CategoryEntity category; // 카테고리
	
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
	private List<CategoryEntity> comments = new ArrayList<>();
	
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
	private List<LikeEntity> likes = new ArrayList<>();
	
	// 생성자, 빌더 등 추가 구현
}
