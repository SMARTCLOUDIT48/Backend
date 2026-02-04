package com.scit48.Inquiry.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Setter
public class InquiryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long inquiryId;
	
	@Column(nullable = false)
	private Long userId;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private InquiryType type;
	
	@Column(nullable = false, length = 200)
	private String title;
	
	@Lob
	@Column(nullable = false)
	private String content;
	
	@Column(name = "attachment_name")
	private String attachmentName;
	
	@Column(name = "attachment_path")
	private String attachmentPath;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private InquiryStatus status;
	
	@Column(nullable = false)
	private boolean isActive;
	
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	// ✅ 여기 추가 ⬇️⬇️⬇️
	public boolean isActive() {
		return isActive;
	}
}


