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
	
	// 1. 유저 정보를 DB에서 가져올 Service를 주입받습니다.
	private final ProfileService ps;
	
	// 2. URL 경로에 있는 {memberId}를 @PathVariable로 받아옵니다.
	@GetMapping("/{memberId}")
	public String userPage (@PathVariable("memberId") String memberId, Model model) {
		
		log.info("조회 요청된 사용자 ID: {}", memberId);
		
		// 3. Service를 통해 해당 memberId를 가진 유저의 전체 정보를 DB에서 조회합니다.
		UserDTO userDTO = ps.findByMemberId(memberId);
		
		// 4. 조회한 유저 정보 객체(userDTO) 전체를 Model에 담아 화면으로 넘깁니다.
		model.addAttribute("user", userDTO);
		
		return "userPage";
	}
}
