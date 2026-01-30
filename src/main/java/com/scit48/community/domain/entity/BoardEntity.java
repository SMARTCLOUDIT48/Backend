package com.scit48.community.domain.entity;

import com.scit48.common.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
	private Long boardId;
	
	@Column(nullable = false)
	private String title;
	
	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;
	
	private int viewCount;
	
	@Column(name = "file_original_name")
	private String fileOriginalName;
	
	@Column(name = "file_name")
	private String fileName;
	
	@Column(name = "file_path")
	private String filePath;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity user; // 작성자
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private CategoryEntity category; // 카테고리
	
	@Builder.Default
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
	private List<CommentEntity> comments = new ArrayList<>();
	
	
	@Builder.Default
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
	private List<LikeEntity> likes = new ArrayList<>();
	
	
	@Column(name = "like_count", columnDefinition = "integer default 0")
	private Integer likeCount;
	
	// 생성자, 빌더 등 추가 구현
	
	// 1. 필드 직접 추가
	private LocalDateTime createdAt;
	
	// 2. 저장 전 자동으로 현재 시간을 세팅하는 메소드
	@CreatedDate // 자동으로 생성 시간 주입
	@Column(name = "created_at", updatable = false)
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		
	}
}
