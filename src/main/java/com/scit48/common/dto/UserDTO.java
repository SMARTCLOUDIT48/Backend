package com.scit48.common.dto;

import com.scit48.common.enums.LanguageLevel;
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

	private String memberId;

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

	private String nativeLanguage;

	private LanguageLevel levelLanguage;

	private LocalDateTime createdAt;

	// --- DTO 변환 메서드 (편의성 제공) ---

	/*
	 * DTO -> Entity 변환
	 * (비밀번호 암호화 처리는 Service 레이어에서 수행 후 별도로 주입하거나,
	 * 여기서는 평문으로 넘기고 Service에서 setter로 덮어쓰는 방식을 사용합니다)
	 */
	public UserEntity toEntity() {
		return UserEntity.builder()
				.id(this.id)
				.memberId(this.memberId)
				.password(this.password)
				.nickname(this.nickname)
				.gender(this.gender)
				.intro(this.intro)
				.age(this.age)
				.nation(this.nation)
				.manner(this.manner)
				.nativeLanguage(this.nativeLanguage)
				.levelLanguage(this.levelLanguage)
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
				.memberId(entity.getMemberId())
				// password는 보안상 Entity에서 가져오더라도 DTO에 담지 않거나,
				// 담더라도 @JsonProperty 설정으로 인해 JSON 응답시 사라집니다.
				.nickname(entity.getNickname())
				.gender(entity.getGender())
				.intro(entity.getIntro())
				.age(entity.getAge())
				.nation(entity.getNation())
				.manner(entity.getManner())
				.nativeLanguage(entity.getNativeLanguage())
				.levelLanguage(entity.getLevelLanguage())
				.profileImageName(entity.getProfileImageName())
				.profileImagePath(entity.getProfileImagePath())
				.createdAt(entity.getCreatedAt())
				.build();
	}

}
