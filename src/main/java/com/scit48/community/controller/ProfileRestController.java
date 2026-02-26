package com.scit48.community.controller;

import com.scit48.auth.member.service.MemberInterestService;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.dto.UserInterestDTO;
import com.scit48.common.enums.InterestType;
import com.scit48.community.service.ProfileService;
import com.scit48.recommend.domain.dto.RecommendDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class ProfileRestController {
	
	private final ProfileService profileService;
	
	// 1. 유저의 '관심사 목록'만 반환하는 API (interestChips 영역용)
	private final MemberInterestService memberInterestService;
	
	@GetMapping("/member/userPage/{memberId}/interests")
	public ResponseEntity<List<UserInterestDTO>> getUserInterests(@PathVariable("memberId") String memberId) {
		
		UserDTO user = profileService.findByMemberId(memberId);
		List<UserInterestDTO> interests = memberInterestService.getUserInterests(user.getId());
		
		log.info("조회된 관심사 개수: {}", interests.size()); // 백엔드 콘솔 창 확인용
		
		return ResponseEntity.ok(interests);
	}
	
}
