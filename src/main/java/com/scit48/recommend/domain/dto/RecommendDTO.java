package com.scit48.recommend.domain.dto;

import com.scit48.common.enums.LanguageLevel;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.enums.Gender;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecommendDTO {

	private Long id; // PK

	private String nickname;

	private Gender gender; // Enum

	private Integer age;

	private String nation;

	private BigDecimal manner;

	private String profileImageName;

	private String profileImagePath;

	private String nativeLanguage;

	private LanguageLevel levelLanguage;

	private int matchPoint;

	// --- DTO 변환 메서드 (편의성 제공) ---

	/*
	 * DTO -> Entity 변환
	 * (비밀번호 암호화 처리는 Service 레이어에서 수행 후 별도로 주입하거나,
	 * 여기서는 평문으로 넘기고 Service에서 setter로 덮어쓰는 방식을 사용합니다)
	 */
	public UserEntity toEntity() {
		return UserEntity.builder()
				.nickname(this.nickname)
				.gender(this.gender)
				.age(this.age)
				.nation(this.nation)
				.manner(this.manner) // 값이 없으면 Entity Builder에서 36.5로 처리됨
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
