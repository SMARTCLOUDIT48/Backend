package com.scit48.chat.controller;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.repository.UserRepository; // 👈 본인 경로 맞는지 확인!

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PageController {
	
	private final UserRepository userRepository;
	
	// ❌ [삭제할 부분] 이 부분이 ChatController와 겹쳐서 에러가 났습니다!
	// @GetMapping("/chat")
	// public String showChatPage() {
	//    return "chat";
	// }
	
	/**
	 * 🎯 상대방 프로필 화면 (userPage.html) 띄워주기
	 */
	@GetMapping("/member/profile/{memberId}")
	public String viewPartnerProfile(@PathVariable("memberId") Long memberId, Model model) {
		
		UserEntity targetUser = userRepository.findById(memberId).orElse(null);
		
		if (targetUser == null) {
			return "redirect:/chat";
		}
		
		model.addAttribute("user", targetUser);
		return "userPage";
	}
}