package com.scit48.common.domain.entity;

import com.scit48.common.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@EntityListeners(AuditingEntityListener.class)     // Auditing 기능 활성화
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;
	
	@Column(name = "email", nullable = false, length = 50, unique = true)
	private String email;
	
	@Column(name = "password", nullable = false)
	private String password;
	
	@Column(name = "nickname", nullable = false, length = 20)
	private String nickname;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "gender", nullable = false)
	private Gender gender;
	
	@Column(name = "intro", columnDefinition = "TEXT")
	private String intro;
	
	@Column(name = "age", nullable = false)
	private Integer age;
	
	@Column(name = "nation", nullable = false, length = 10)
	private String nation;
	
	// 초기값 36.5 설정
	@Column(name = "manner", nullable = false, precision = 4, scale = 1)
	@ColumnDefault("36.5")
	private BigDecimal manner;
	
	@Column(name = "native_language")
	private String nativeLanguage;
	
	@Column(name = "level_language")
	private String levelLanguage;
	
	@Column(name = "profile_image_name")
	private String profileImageName;
	
	@Column(name = "profile_image_path")
	private String profileImagePath;
	
	@CreatedDate
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	// --- Builder 생성자 ---
	@Builder
	public UserEntity(String email, String password, String nickname, Gender gender,
					  String intro, Integer age, String nation, BigDecimal manner,
					  String nativeLanguage, String levelLanguage,
					  String profileImageName, String profileImagePath) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.gender = gender;
		this.intro = intro;
		this.age = age;
		this.nation = nation;
		this.nativeLanguage = nativeLanguage;
		this.levelLanguage = levelLanguage;
		
		// manner 값이 입력되지 않았다면(null) 기본값 36.5를 할당
		this.manner = (manner != null) ? manner : BigDecimal.valueOf(36.5);
		
		this.profileImageName = profileImageName;
		this.profileImagePath = profileImagePath;
	}
	
	// 프로필 사진 변경을 위한 비즈니스 메서드
	public void updateProfileImage(String profileImageName, String profileImagePath) {
		this.profileImageName = profileImageName;
		this.profileImagePath = profileImagePath;
	}
	
}
