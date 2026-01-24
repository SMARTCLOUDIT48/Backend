package com.scit48.member.controller;

import com.scit48.member.dto.MyPageResponse;
import com.scit48.member.entity.MemberEntity;
import com.scit48.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/me")
    public MyPageResponse myPage(@AuthenticationPrincipal User user) {

        long memberId = Long.valueOf(user.getUsername());

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow();

        return new MyPageResponse(
                member.getMemberId(),
                member.getNickname(),
                member.getGender(),
                member.getAge(),
                member.getNation(),
                member.getManner().doubleValue(),
                member.getIntro(),
                member.getProfileImagePath());
    }
}
