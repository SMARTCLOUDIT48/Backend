package com.scit48.recommend.controller;

import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.chat.domain.dto.DirectRoomResponseDTO;
import com.scit48.chat.service.ChatRoomMemberService;
import com.scit48.common.dto.UserDTO;
import com.scit48.recommend.domain.dto.MatchResponseDTO;
import com.scit48.recommend.domain.dto.RecommendDTO;
import com.scit48.recommend.service.MatchService;
import com.scit48.recommend.service.RecommendService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RecommendController {
	
	private  final RecommendService rs;
	private  final ChatRoomMemberService chatRoomMemberService;
	private  final MatchService matchService;
	
	@Getter
	@Setter
	public static class CriteriaKey {
		private String criteriaKey;
	}
	
	/**
	 * Recommend 페이지로 이동
	 * @return
	 */
	@GetMapping("recommend") //임시
	public String Recommend(){
		
		return "recommend";
	}
	
	@ResponseBody
	@GetMapping("/api/recommend")
	public List<RecommendDTO> recommend(
			@AuthenticationPrincipal UserDetails user
	){
		Long user_id = rs.searchid(user);
		List<RecommendDTO> userDTO= rs.firstRecommend(user_id);
		return userDTO;
	}
	
	@ResponseBody
	@PostMapping("/api/chat/rooms/direct/{partnerId}")
	public DirectRoomResponseDTO directRoom(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long partnerId
	){
		Long myId = rs.searchid(userDetails);
		DirectRoomResponseDTO dto = chatRoomMemberService.createOrGetDirectRoom(myId,partnerId);
		return dto;
	}
	
	@ResponseBody
	@PostMapping("/api/match/start")
	public MatchResponseDTO start(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody(required = false) CriteriaKey req){
		Long myId = rs.searchid(userDetails);
		String criteriaKey = (req != null) ? req.getCriteriaKey() : null;
		log.debug("match 필터링키 : {}", criteriaKey);
		return matchService.start(myId, criteriaKey);
	}
	
	@ResponseBody
	@GetMapping("/api/match/result")
	public MatchResponseDTO result(
			@AuthenticationPrincipal UserDetails userDetails
	){
		Long myId = rs.searchid(userDetails);
		return matchService.getOrWaiting(myId);
	}
	
	@PostMapping("/api/match/cancel")
	public ResponseEntity<?> cancel(@AuthenticationPrincipal UserDetails userDetails) {
		Long myId = rs.searchid(userDetails);
		matchService.cancel(myId);
		return ResponseEntity.ok(Map.of("status", "CANCELED"));
	}
	
	@ResponseBody
	@GetMapping("api/filtering/search")
	public List<RecommendDTO> recommendFiltering(
			@AuthenticationPrincipal UserDetails user,
			@RequestParam(required = false) String criteriaKey
	){
		Long user_id = rs.searchid(user);
		System.out.println("log 안뜸");
		log.debug("parsing 되지 않은 필터링 키 : {}",criteriaKey);
		List<RecommendDTO> userDTO= rs.filteringSearch(user_id, criteriaKey);
		return userDTO;
	}
	
}
