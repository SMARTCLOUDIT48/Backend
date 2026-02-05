package com.scit48.admin.controller;

import com.scit48.admin.service.AdminPostService;
import com.scit48.community.domain.entity.BoardEntity;
import com.scit48.community.repository.BoardRepository;
import com.scit48.notice.domain.entity.NoticeEntity;
import com.scit48.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/posts")
public class AdminPostController {
	
	private final AdminPostService adminPostService;
	private final BoardRepository boardRepository;
	private final NoticeRepository noticeRepository;
	
	@GetMapping
	public String postList(
			@RequestParam(defaultValue = "ALL") String board,
			@RequestParam(defaultValue = "TITLE") String searchType,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "0") int page,
			Model model
	) {
		var result = adminPostService.getPosts(board, searchType, keyword, page);
		
		model.addAttribute("posts", result.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", result.getTotalPages());
		
		model.addAttribute("board", board);
		model.addAttribute("searchType", searchType);
		model.addAttribute("keyword", keyword);
		
		return "admin/post-list";
	}
	
	@PostMapping("/delete")
	public String delete(
			@RequestParam String board,
			@RequestParam Long id
	) {
		adminPostService.deletePost(board, id);
		return "redirect:/admin/posts";
	}
	@GetMapping("/{id}")
	@ResponseBody
	public Map<String, String> getPostDetail(
			@PathVariable Long id,
			@RequestParam String board
	) {
		String title;
		String content;
		
		if ("FAQ".equals(board) || "NOTICE".equals(board)) {
			NoticeEntity notice = noticeRepository.findById(id)
					.orElseThrow();
			title = notice.getTitle();
			content = notice.getContent();
		} else {
			BoardEntity boardPost = boardRepository.findById(id)
					.orElseThrow();
			title = boardPost.getTitle();
			content = boardPost.getContent();
		}
		
		Map<String, String> result = new HashMap<>();
		result.put("title", title);
		result.put("content", content);
		return result;
	}
	
	@PostMapping("/update")
	@ResponseBody
	public Map<String, Object> updatePost(
			@RequestParam Long id,
			@RequestParam String board,
			@RequestParam String title,
			@RequestParam String content
	) {
		if ("FAQ".equals(board) || "NOTICE".equals(board)) {
			NoticeEntity notice = noticeRepository.findById(id)
					.orElseThrow();
			notice.setTitle(title);
			notice.setContent(content);
			noticeRepository.save(notice);
		} else {
			BoardEntity post = boardRepository.findById(id)
					.orElseThrow();
			post.setTitle(title);
			post.setContent(content);
			boardRepository.save(post);
		}
		
		Map<String, Object> result = new HashMap<>();
		result.put("success", true);
		return result;
	}
	
	
	
}
