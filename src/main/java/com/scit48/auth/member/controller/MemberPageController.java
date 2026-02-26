package com.scit48.auth.member.controller;

import com.scit48.community.domain.dto.BoardDTO;
import com.scit48.community.domain.entity.CategoryEntity;
import com.scit48.community.repository.CategoryRepository;
import com.scit48.community.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberPageController {

	private final BoardService bs;
	private final CategoryRepository ctr;
	
    // 마이페이지 메인
    @GetMapping("/mypage")
    public String myPage() {
        return "member/mypage";
    }
	
	// 마이페이지 내 게시글 목록
	@GetMapping("/mypage/myBoardList")
	public String myBoardList(
			@AuthenticationPrincipal UserDetails user,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "cateName", required = false) String cateName,
			@RequestParam(name = "searchType", required = false) String searchType,
			@RequestParam(name = "keyword", required = false) String keyword,
			Model model) {
		
		String loginId = user.getUsername();
		
		Page<BoardDTO> boardList = bs.getMyBoardList(loginId, page, cateName, searchType, keyword);
		List<CategoryEntity> categories = ctr.findAll();
		
		int startPage = Math.max(1, boardList.getNumber() - 4);
		int endPage = Math.min(boardList.getTotalPages(), boardList.getNumber() + 4);
		if (endPage == 0) endPage = 1;
		
		model.addAttribute("boardList", boardList);
		model.addAttribute("categories", categories);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		
		model.addAttribute("cateName", cateName);
		model.addAttribute("searchType", searchType);
		model.addAttribute("keyword", keyword);
		
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