package com.scit48.notice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NoticeEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long noticeId;
	
	@Column(nullable = false, length = 20)
	private String type;

	
	@Column(nullable = false, length = 200)
	private String title;
	
	@Lob
	@Column(nullable = false)
	private String content;
	
	@Column(nullable = false, length = 20)
	private String category;
	
	@Builder.Default
	@Column(name = "is_pinned", nullable = false)
	private boolean isPinned = false;
	
	@Builder.Default
	@Column(nullable = false)
	private Integer viewCount = 0;
	
	@Builder.Default
	@Column(nullable = false)
	private Integer sortOrder = 0;
	
	@Builder.Default
	@Column(name = "is_active", nullable = false)
	private boolean isActive = true;
	
	@Column(nullable = false)
	private Long createdBy;
	
	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	public void update(String title, String content) {
		this.title = title;
		this.content = content;
	}
	
	public void setActive(boolean active) {
		this.isActive = active;
	}
	
}

