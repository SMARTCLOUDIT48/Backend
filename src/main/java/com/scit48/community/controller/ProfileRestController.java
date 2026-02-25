package com.scit48.community.controller;

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
	@GetMapping("/members/{memberId}/interests")
	public ResponseEntity<List<InterestType>> getUserInterests(@PathVariable("memberId") String memberId) {
		log.info("API 요청: {} 님의 관심사 목록 조회", memberId);
		
		List<InterestType> interests = profileService.getUserInterests(memberId);
		return ResponseEntity.ok(interests);
	}
	
	// 2. '추천 유저 목록'을 반환하는 API (recommendGrid 영역용)
	@GetMapping("/recommend/{memberId}")
	public ResponseEntity<List<RecommendDTO>> getRecommendations(@PathVariable("memberId") String memberId) {
		log.info("API 요청: {} 님의 추천 유저 목록 조회", memberId);
		
		List<RecommendDTO> recommendedUsers = profileService.getRecommendedUsers(memberId);
		return ResponseEntity.ok(recommendedUsers);
	}
}
