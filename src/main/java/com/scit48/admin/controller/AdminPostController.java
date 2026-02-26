package com.scit48.admin.controller;

import com.scit48.admin.service.AdminPostService;
import com.scit48.community.domain.entity.BoardEntity;
import com.scit48.community.repository.BoardRepository;
import com.scit48.notice.domain.entity.NoticeEntity;
import com.scit48.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/posts")
public class AdminPostController {
	
	private final AdminPostService adminPostService;
	private final BoardRepository boardRepository;
	private final NoticeRepository noticeRepository;
	
	@Value("${board.uploadPath}")
	private String boardUploadPath;
	
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
		Map<String, String> result = new HashMap<>();
		
		if ("FAQ".equals(board) || "NOTICE".equals(board)) {
			
			NoticeEntity notice = noticeRepository.findById(id).orElseThrow();
			result.put("title", notice.getTitle());
			result.put("content", notice.getContent());
			result.put("imagePath", null);
			
		} else {
			
			BoardEntity boardPost = boardRepository.findById(id).orElseThrow();
			result.put("title", boardPost.getTitle());
			result.put("content", boardPost.getContent());
			result.put("imagePath", boardPost.getFilePath()); // 🔥 이 줄 추가
		}
		
		return result;
	}
	
	@PostMapping("/update")
	@ResponseBody
	public Map<String, Object> updatePost(
			@RequestParam Long id,
			@RequestParam String board,
			@RequestParam String title,
			@RequestParam String content,
			@RequestParam(required = false) MultipartFile image
	) throws Exception {
		
		String uploadDir = boardUploadPath;
		
		 // 네가 쓰는 실제 경로로 수정
		
		if ("FAQ".equals(board) || "NOTICE".equals(board)) {
			
			NoticeEntity notice = noticeRepository.findById(id).orElseThrow();
			notice.setTitle(title);
			notice.setContent(content);
			
			// 공지에 이미지 필드 있다면 여기도 동일 처리
			
			noticeRepository.save(notice);
			
		} else {
			BoardEntity post = boardRepository.findById(id).orElseThrow();
			
			post.setTitle(title);
			post.setContent(content);

// 🔥 이미지 새 업로드 했을 때만 교체
			if (image != null && !image.isEmpty()) {
				
				String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
				File saveFile = new File(boardUploadPath, fileName);
				image.transferTo(saveFile);
				
				post.setFilePath("/files/" + fileName);
				post.setFileOriginalName(image.getOriginalFilename());
			}
			
			boardRepository.save(post);
		}
		
		Map<String, Object> result = new HashMap<>();
		result.put("success", true);
		return result;
	}
	
	
	
}
