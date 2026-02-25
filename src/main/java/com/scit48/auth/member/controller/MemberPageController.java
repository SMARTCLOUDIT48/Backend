package com.scit48.auth.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
public class MemberPageController {

    // 마이페이지 메인
    @GetMapping("/mypage")
    public String myPage() {
        return "member/mypage";
    }
	
	// 마이페이지 내 게시글 목록
	@GetMapping("/mypage/myBoardList")
	public String myBoardList () {
		
		return "myBoardList";
	}

    // 회원가입
    @GetMapping("/signup")
    public String signupPage() {
        return "member/signup";
    }

    // 관심사
    @GetMapping("/interest")
    public String interestPage() {
        return "member/interest";
    }
}