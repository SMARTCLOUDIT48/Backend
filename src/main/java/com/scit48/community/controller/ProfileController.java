package com.scit48.community.controller;

import com.scit48.common.dto.UserDTO;
import com.scit48.community.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member/userPage")
public class ProfileController {
	
	private final ProfileService ps;
	
	@GetMapping("/{memberId}")
	public String userPage (@PathVariable("memberId") String memberId, Model model) {
		
		log.info("조회 요청된 사용자 ID: {}", memberId);
		
		// Service를 통해 해당 memberId를 가진 유저의 전체 정보를 DB에서 조회합니다.
		UserDTO userDTO = ps.findByMemberId(memberId);
		
		// 조회한 유저 정보 객체(userDTO) 전체를 Model에 담아 화면으로 넘깁니다.
		model.addAttribute("user", userDTO);
		
		return "userPage";
	}
	
	@GetMapping("/{memberId}/userBoardList")
	public String userBoardList (@PathVariable("memberId") String memberId) {
		
		return "userBoardList";
	}
	
	
}
