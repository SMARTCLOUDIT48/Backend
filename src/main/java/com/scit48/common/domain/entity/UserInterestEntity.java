package com.scit48.common.domain.entity;

import com.scit48.common.enums.InterestDetail;
import com.scit48.common.enums.InterestType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
		name = "user_interest",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_user_interest",
						columnNames = {"user_id", "interest", "interest_detail"}
				)
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserInterestEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_interest_id")
	private Long id; // interest_id
	
	//@ManyToOne(fetch = FetchType.LAZY)
	//@JoinColumn(name = "user_id", nullable = false)
	//private User user; //user > user_id 호출
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private InterestType interest;   // 대분류
	
	@Enumerated(EnumType.STRING)
	@Column(name = "interest_detail", nullable = false, length = 100)
	private InterestDetail interestDetail; // 세부 관심사
}