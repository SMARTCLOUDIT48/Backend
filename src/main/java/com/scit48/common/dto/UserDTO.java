package com.scit48.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scit48.common.enums.Gender;
import com.scit48.common.domain.entity.UserEntity;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDTO {
	
	private Long id; // PK
	
	private String email;
	
	// 중요: 클라이언트 -> 서버 전송(입력) 시에는 동작하지만,
// 서버 -> 클라이언트 응답(조회) 시에는 JSON에서 제외됩니다.
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;
	
	private String nickname;
	
	private Gender gender; // Enum
	
	private String intro;
	
	private Integer age;
	
	private String nation;
	
	private BigDecimal manner;
	
	private String profileImageName;
	
	private String profileImagePath;
	
	private LocalDateTime createdAt;

// --- DTO 변환 메서드 (편의성 제공) ---
	
	/*
	 * DTO -> Entity 변환
	 * (비밀번호 암호화 처리는 Service 레이어에서 수행 후 별도로 주입하거나,
	 * 여기서는 평문으로 넘기고 Service에서 setter로 덮어쓰는 방식을 사용합니다)
	 */
	public UserEntity toEntity() {
		return UserEntity.builder()
				.email(this.email)
				.password(this.password) // 암호화 전 평문 혹은 암호화된 값 (Service 로직에 따라 다름)
				.nickname(this.nickname)
				.gender(this.gender)
				.intro(this.intro)
				.age(this.age)
				.nation(this.nation)
				.manner(this.manner) // 값이 없으면 Entity Builder에서 36.5로 처리됨
				.profileImageName(this.profileImageName)
				.profileImagePath(this.profileImagePath)
				.build();
	}
	
	/*
	 * Entity -> DTO 변환
	 */
	public static UserDTO fromEntity(UserEntity entity) {
		return UserDTO.builder()
				.id(entity.getId())
				.email(entity.getEmail())
				// password는 보안상 Entity에서 가져오더라도 DTO에 담지 않거나,
				// 담더라도 @JsonProperty 설정으로 인해 JSON 응답시 사라집니다.
				.nickname(entity.getNickname())
				.gender(entity.getGender())
				.intro(entity.getIntro())
				.age(entity.getAge())
				.nation(entity.getNation())
				.manner(entity.getManner())
				.profileImageName(entity.getProfileImageName())
				.profileImagePath(entity.getProfileImagePath())
				.createdAt(entity.getCreatedAt())
				.build();
	}
	
}

