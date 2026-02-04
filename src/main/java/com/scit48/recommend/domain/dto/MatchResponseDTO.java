package com.scit48.recommend.domain.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponseDTO {
	
	private String status; // "WAITING" / "MATCHED"
	private Long roomId;
	private String roomUuid;
	private Long partnerId;
	
	public static MatchResponseDTO waiting(){
		return MatchResponseDTO.builder().status("WAITING").build();
	}
	
	public static MatchResponseDTO matched(Long roomId, String roomUuid, Long partnerId){
		return MatchResponseDTO.builder()
				.status("MATCHED")
				.roomId(roomId)
				.roomUuid(roomUuid)
				.partnerId(partnerId)
				.build();
	}
}
