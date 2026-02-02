package com.scit48.common.dto;

import com.scit48.common.domain.entity.UserInterestEntity;
import com.scit48.common.enums.InterestDetail;
import com.scit48.common.enums.InterestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInterestDTO {

	// 응답 시에만 사용
	private Long userInterestId;

	// 코드값 (요청/응답 공용)
	private InterestType interest; // STUDY
	private InterestDetail interestDetail; // LANGUAGE_STUDY

	// 화면 표시용 (응답 전용)
	private String interestLabel; // 학습·자기계발
	private String interestDetailLabel; // 언어 학습

	/*
	 * =========================
	 * Entity → DTO 변환
	 * =========================
	 */
	public static UserInterestDTO fromEntity(UserInterestEntity entity) {
		return UserInterestDTO.builder()
				.userInterestId(entity.getId())
				.interest(entity.getInterest())
				.interestDetail(entity.getInterestDetail())
				.interestLabel(entity.getInterest().getValue())
				.interestDetailLabel(entity.getInterestDetail().getValue())
				.build();
	}
}
