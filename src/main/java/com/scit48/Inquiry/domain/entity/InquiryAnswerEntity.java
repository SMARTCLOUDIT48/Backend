package com.scit48.Inquiry.domain.entity;

import com.scit48.common.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry_answer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InquiryAnswerEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long answerId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "inquiry_id", nullable = false, unique = true)
	private InquiryEntity inquiry;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id", nullable = false)
	private UserEntity admin;
	
	@Lob
	@Column(nullable = false)
	private String content;
	
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	// ⭐ 답변 수정용 메서드
	public void updateContent(String content) {
		this.content = content;
	}
}
