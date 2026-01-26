package com.scit48.recommend.controller;

import com.scit48.common.dto.UserDTO;
import com.scit48.recommend.domain.dto.RecommendDTO;
import com.scit48.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RecommendController {
	
	private  final RecommendService rs;
	
	
	@GetMapping("recommend") //임시
	public String Recommend(@AuthenticationPrincipal UserDetails user){
		Long user_id = Long.valueOf(user.getUsername());
		List<RecommendDTO> userDTO= rs.firstRecommend(user_id);
		
		return "recommend";
	}
}
