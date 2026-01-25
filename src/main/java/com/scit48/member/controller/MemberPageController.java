package com.scit48.member.controller;

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

    @GetMapping("/signup")
    public String signupPage() {
        return "member/signup";
    }

    @GetMapping("/interest")
    public String interestPage() {
        return "member/interest";
    }
}