package com.scit48.community.controller;

import com.scit48.community.domain.dto.BoardDTO;
import com.scit48.community.domain.entity.CategoryEntity;
import com.scit48.community.repository.CategoryRepository;
import com.scit48.community.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("board")
public class BoardController {
	
	private final BoardService bs;
	private final CategoryRepository cr;
	
	// application.properties 파일의 설정값
	@Value("${board.pageSize}")
	int pageSize;
	
	@Value("${board.linkSize}")
	int linkSize;
	
	@Value("${board.uploadPath}")
	String uploadPath;
	
	@GetMapping("write")
	public String write (Model model) {
		
		// 카테고리 선택창을 위해 DB에서 카테고리 목록을 가져와서 뷰로 전달
		List<CategoryEntity> categories = cr.findAll();
		model.addAttribute("categories", categories);
		
		return "boardWrite";
	}
	
	@PostMapping("write")
	public String write(
			BoardDTO boardDTO
			, @AuthenticationPrincipal UserDetails user
			, @RequestParam(name = "upload", required = false)
			MultipartFile upload, Model model
	) {
		
		// 작성한 글 정보에 사용자 아이디 추가
		boardDTO.setId(Long.valueOf(user.getUsername()));
		log.debug("저장할 글 정보: {}", boardDTO);
		
		// 업로드된 첨부파일
		if (upload != null) {
			log.debug("Empty: {}"		, upload.isEmpty());
			log.debug("파라미터 이름: {}"	, upload.getName());
			log.debug("파일명: {}"		, upload.getOriginalFilename());
			log.debug("파일크기: {}"		, upload.getSize());
			log.debug("파일종류: {}"		, upload.getContentType());
		}
		
		try {
			bs.write(boardDTO, uploadPath, upload);
		} catch (Exception e) {
			log.debug("[예외 발생] {}", e.getMessage());
			
			// 에러 발생 시 카테고리 목록을 다시 담아줘야 페이지가 정상 출력됨
			List<CategoryEntity> categories = cr.findAll();
			model.addAttribute("categories", categories);
			model.addAttribute("error", "글 저장 중 오류가 발생했습니다.");
			
			return "boardWrite";
		}
		
		return "redirect:/community";	//임시
	}
	
	@GetMapping("list")
	public String list() {
		return "boardList";
	}

}
