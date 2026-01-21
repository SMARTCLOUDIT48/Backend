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
	
	// 응답 시에만 사용 (요청 시 null 허용)
	private Long userInterestId;
	
	// 요청 / 응답 공용
	private InterestType interest;
	private InterestDetail interestDetail;
	
	/* =========================
	   Entity → DTO 변환
	   ========================= */
	public static UserInterestDTO fromEntity(UserInterestEntity entity) {
		return UserInterestDTO.builder()
				.userInterestId(entity.getId())
				.interest(entity.getInterest())
				.interestDetail(entity.getInterestDetail())
				.build();
	}
}