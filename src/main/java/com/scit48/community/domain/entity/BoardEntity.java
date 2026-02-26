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
	
	
	@Builder.Default
	@Column(name = "like_cnt", nullable = false)
	private Integer likeCnt = 0;
	
	
	@CreatedDate
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	// [수정] 비즈니스 로직 (좋아요 증가/감소 메서드)
	// ==========================================
	
	// 좋아요 1 증가
	public void increaseLikeCount() {
		if (this.likeCnt == null) {
			this.likeCnt = 0;
		}
		this.likeCnt++;
	}
	
	// 좋아요 1 감소 (0 밑으로 내려가지 않도록 방어)
	public void decreaseLikeCount() {
		if (this.likeCnt == null) {
			this.likeCnt = 0;
		}
		if (this.likeCnt > 0) {
			this.likeCnt--;
		}
	}
}
