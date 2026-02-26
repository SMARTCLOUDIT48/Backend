package com.scit48.community.controller;

import com.scit48.common.dto.UserDTO;
import com.scit48.community.domain.dto.BoardDTO;
import com.scit48.community.service.BoardService;
import com.scit48.community.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/member/userPage")
public class ProfileController {

	private final ProfileService ps;
	private final BoardService bs;

	@GetMapping("/{memberId}")
	public String userPage(@PathVariable("memberId") String memberId,
			@RequestParam(value = "page", defaultValue = "1") int page, // 기본 1페이지
			@RequestParam(value = "searchType", defaultValue = "") String searchType, // title, content
			@RequestParam(value = "searchKeyword", defaultValue = "") String searchKeyword,
			Model model) {

		log.info("조회 요청된 사용자 ID: {}, 검색조건: {}, 검색어: {}", memberId, searchType, searchKeyword);

		UserDTO userDTO = ps.findByMemberId(memberId);
		model.addAttribute("user", userDTO);

		Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<BoardDTO> boardPage = ps.getUserBoards(memberId, pageable, searchType, searchKeyword);

		model.addAttribute("boardPage", boardPage);
		model.addAttribute("searchType", searchType); // 페이지 이동 시 검색 조건 유지를 위해 전달
		model.addAttribute("searchKeyword", searchKeyword); // 페이지 이동 시 검색어 유지를 위해 전달
		model.addAttribute("totalCount", boardPage.getTotalElements()); // 게시글 조회

		return "userPage";
	}

	/**
	 * 유저 페이지 내에서 특정 게시글 상세 보기
	 */
	@GetMapping("/{memberId}/read/{boardId}")
	public String readUserBoard(
			@PathVariable("memberId") String memberId,
			@PathVariable("boardId") Long boardId,
			Model model) {

		log.info("{} 유저 페이지에서 게시글 {} 조회 요청", memberId, boardId);

		BoardDTO boardDTO = bs.read(boardId);

		model.addAttribute("board", boardDTO);
		model.addAttribute("memberId", memberId); // '목록으로 돌아가기' 버튼 등을 위해 넘겨줌

		return "boardRead";
	}

}
