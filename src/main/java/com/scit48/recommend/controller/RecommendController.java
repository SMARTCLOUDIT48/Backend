package com.scit48.recommend.controller;

import com.scit48.chat.domain.dto.DirectRoomResponseDTO;
import com.scit48.chat.service.ChatRoomMemberService;
import com.scit48.common.dto.UserDTO;
import com.scit48.recommend.domain.dto.RecommendDTO;
import com.scit48.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RecommendController {
	
	private  final RecommendService rs;
	private  final ChatRoomMemberService chatRoomMemberService;
	
	
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
		log.debug(userDetails.getUsername());
		DirectRoomResponseDTO dto = chatRoomMemberService.createOrGetDirectRoom(myId,partnerId);
		return dto;
	}
}
