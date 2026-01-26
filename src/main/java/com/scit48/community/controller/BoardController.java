package com.scit48.community.controller;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.repository.UserRepository;
import com.scit48.community.domain.dto.BoardDTO;
import com.scit48.community.domain.entity.CategoryEntity;
import com.scit48.community.repository.CategoryRepository;
import com.scit48.community.service.BoardService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("board")
public class BoardController {
	
	private final BoardService bs;
	private final CategoryRepository cr;
	private final UserRepository ur;
	
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
		model.addAttribute("boardDTO", new BoardDTO());
		
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
		
		return "redirect:/board/list";	//추후 구현
	}
	
	
	@GetMapping("feedWrite")
	public String feedWrite(//@AuthenticationPrincipal UserDetails user,
							Model model) {
		/*
		// 1. 로그인이 되어있는지 명시적으로 확인
		if (user == null) {
			log.debug("비로그인 사용자의 접근 - 로그인 페이지로 리다이렉트");
			return "redirect:/user/login"; // 로그인 페이지 경로로 수정하세요
		}
		
		// 2. 로그인한 사용자 ID 추출 및 DB 조회
		Long userId = Long.valueOf(user.getUsername());
		UserEntity userEntity = ur.findById(userId).orElseThrow(
				() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId)
		);
		
		// 3. 'UserDetails'가 아닌 'UserEntity'를 모델에 담기 (HTML 에러 방지)
		model.addAttribute("user", userEntity);
		*/
		return "feedWrite"; // SNS 스타일 전용 템플릿
	}
	
	
	@PostMapping("feedWrite")
	public String feedWrite(
			BoardDTO boardDTO
			, @AuthenticationPrincipal UserDetails user
			, @RequestParam(name = "upload", required = false)
			MultipartFile upload
	) {
		
		// 작성한 글 정보에 사용자 아이디 추가
		boardDTO.setId(Long.valueOf(user.getUsername()));
		log.debug("저장할 피드글 정보: {}", boardDTO);
		
		// 업로드된 첨부파일
		if (upload != null) {
			log.debug("Empty: {}"		, upload.isEmpty());
			log.debug("파라미터 이름: {}"	, upload.getName());
			log.debug("파일명: {}"		, upload.getOriginalFilename());
			log.debug("파일크기: {}"		, upload.getSize());
			log.debug("파일종류: {}"		, upload.getContentType());
		}
		try {
			// '일상' 카테고리 자동 지정 (DB에 'DAILY' 또는 '일상'이라는 이름의 카테고리가 있다고 가정)
			CategoryEntity dailyCategory = cr.findByName("일상") // 혹은 "DAILY"
					.orElseThrow(() -> new IllegalArgumentException("일상 카테고리가 DB에 없습니다."));
			
			boardDTO.setCategoryId(dailyCategory.getCategoryId());
			bs.feedWrite(boardDTO, uploadPath, upload);
		} catch (IOException e) {
			log.debug("예외 발생: {}", e.getMessage());
			return "feedWrite";
		}
		
		return "redirect:/board/feedView"; // 피드 목록으로 이동 (추후 구현)
	}
	
	
	
	@GetMapping("list")
	public String list() {
		return "boardList";
	}
	
}
