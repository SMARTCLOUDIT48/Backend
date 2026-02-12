package com.scit48.community.controller;

import com.scit48.common.dto.UserDTO;
import com.scit48.common.repository.UserRepository;
import com.scit48.community.domain.dto.BoardDTO;
import com.scit48.community.domain.dto.LikeDTO;
import com.scit48.community.domain.entity.BoardEntity;
import com.scit48.community.domain.entity.CategoryEntity;
import com.scit48.community.repository.BoardRepository;
import com.scit48.community.repository.CategoryRepository;
import com.scit48.community.service.BoardService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
	private final BoardRepository br;
	
	// application.properties 파일의 설정값
	@Value("${board.pageSize}")
	int pageSize;
	
	@Value("${board.linkSize}")
	int linkSize;
	
	@Value("${board.uploadPath}")
	String uploadPath;
	
	@GetMapping("write")
	public String write (@AuthenticationPrincipal UserDetails user, Model model, BoardDTO boardDTO) {
		
		// 1. 서비스 호출 (비즈니스 로직 위임)
		UserDTO userDTO = bs.getUserInfo(user);
		
		// 2. 결과에 따른 분기 처리
		if (userDTO == null) {
			// 로그인이 안 된 경우
			return "redirect:/login"; // 임시이므로 제대로 된 로그인 화면 경로로 수정할 것
		}
		
		// 카테고리 선택창을 위해 DB에서 카테고리 목록을 가져와서 뷰로 전달
		List<CategoryEntity> categories = cr.findAll();
		model.addAttribute("categories", categories);
		model.addAttribute("userDTO", userDTO);
		
		
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
		boardDTO.setMemberId(user.getUsername());
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
			log.debug("예외 발생: {}", e.getMessage());
			
			
			// 에러 발생 시 카테고리 목록을 다시 담아줘야 페이지가 정상 출력됨
			List<CategoryEntity> categories = cr.findAll();
			model.addAttribute("categories", categories);
			model.addAttribute("error", "글 저장 중 오류가 발생했습니다.");
			
			
			return "redirect:/board/write";
		}
		
		return "redirect:/board/read/" + boardDTO.getBoardId();
	}
	
	
	@GetMapping("feedWrite")
	public String feedWrite(@AuthenticationPrincipal UserDetails user,
							Model model) {
		
		// 1. 서비스 호출 (비즈니스 로직 위임)
		UserDTO userDTO = bs.getUserInfo(user);
		
		// 2. 결과에 따른 분기 처리
		if (userDTO == null) {
			// 로그인이 안 된 경우
			return "redirect:/login"; // 임시이므로 제대로 된 로그인 화면 경로로 수정할 것
		}
		
		// 3. 모델에 DTO 담기
		model.addAttribute("user", userDTO);
		
		// 4. 뷰 반환
		return "feedWrite";
	}
	
	
	@PostMapping("feedWrite")
	public String feedWrite(
			BoardDTO boardDTO
			, @AuthenticationPrincipal UserDetails user
			, @RequestParam(name = "upload", required = false)
			MultipartFile upload
	) {
		
		// 작성한 글 정보에 사용자 아이디 추가
		boardDTO.setMemberId(user.getUsername());
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
			bs.feedWrite(boardDTO, uploadPath, upload);
		} catch (Exception e) {
			log.debug("예외 발생: {}", e.getMessage());
			return "redirect:/board/feedWrite";
		}
		
		return "redirect:/board/feedView"; // 피드 목록으로 이동 (추후 구현)
	}
	
	
	
	@GetMapping("list")
	public String list(
			Model model,
			@PageableDefault(page = 1, size = 10, sort = "boardId", direction = Sort.Direction.DESC)
			Pageable pageable,
			@RequestParam(required = false) String cateName,       // 카테고리 필터
			@RequestParam(required = false) String searchType, // title, content, writer
			@RequestParam(required = false) String keyword) {  // 검색어
		
		
		
		// 카테고리 목록 (필터 선택용, '일상' 제외하고 가져오기)
		List<CategoryEntity> categories = cr.findByNameNot("일상");
		model.addAttribute("categories", categories);
		
		// 검색 로직 수행
		Page<BoardDTO> boardList = bs.searchPosts(pageable, cateName, searchType, keyword);
		
		
		// 페이징 블록 계산
		int blockLimit = 5;
		int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) - 1) * blockLimit + 1;
		int endPage = ((startPage + blockLimit - 1) < boardList.getTotalPages()) ? startPage + blockLimit - 1 : boardList.getTotalPages();
		
		model.addAttribute("boardList", boardList);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		
		// 검색 상태 유지를 위해 모델에 추가
		model.addAttribute("cateName", cateName);
		model.addAttribute("searchType", searchType);
		model.addAttribute("keyword", keyword);
		
		return "boardList";
	}
	
	@GetMapping("feedView")
	public String feedView(
			Model model,
			@PageableDefault(page = 0, size = 5, sort = "boardId",
					direction = Sort.Direction.DESC) Pageable pageable
	) {
		// 1. '일상' 카테고리 글만 조회
		Page<BoardEntity> feeds = br.findByCategoryName("일상", pageable);
		
		// 2. Entity -> DTO 변환
		Page<BoardDTO> feedList = feeds.map(board -> BoardDTO.builder()
				.id(board.getUser().getId())
				.title(board.getTitle())
				.content(board.getContent())
				.boardId(board.getBoardId())
				.writerNickname(board.getUser().getNickname())
				.profileImagePath(board.getUser().getProfileImagePath())// 프로필 이미지
				.profileImageName("/images/profile/upload/" + board.getUser().getProfileImageName())
				.fileName(board.getFileName())
				.filePath(board.getFilePath()) // ★ 중요: 피드 이미지
				.likeCnt(board.getLikeCnt() != null ? board.getLikeCnt() : 0)
				.createdDate(board.getCreatedAt())
				.nation(board.getUser().getNation())
				.memberId(board.getUser().getMemberId())
				.build());
		
		
		model.addAttribute("feedList", feedList);
		
		return "feedView";
	}
	
	@GetMapping("/read/{boardId}")
	public String read(@PathVariable Long boardId, Model model, HttpSession session) {
		// 서비스에서 데이터 가져오기
		BoardDTO boardDTO = bs.readUpdate(boardId);
		
		// 현재 로그인한 사용자 ID (수정/삭제 버튼 표시 여부 확인용)
		Long loginUserId = (Long) session.getAttribute("loginUserId");
		
		// (선택 사항) 좋아요 여부 확인 로직이 있다면 여기서 boolean isLiked 등을 모델에 담음
		
		model.addAttribute("board", boardDTO);
		model.addAttribute("loginUserId", loginUserId); // 뷰에서 본인 확인용
		
		return "boardRead";
	}
	
	
	@GetMapping("/update/{boardId}")
	public String update(@PathVariable Long boardId, Model model) {
		// 기존 데이터를 조회해서 폼에 채워넣기 위해 DTO를 가져옵니다.
		BoardDTO boardDTO = bs.findById(boardId);
		model.addAttribute("board", boardDTO);
		return "boardUpdate";
	}
	
	@PostMapping("/update")
	public String update(@ModelAttribute BoardDTO boardDTO,
						 @RequestParam(name = "file", required = false) MultipartFile file) throws IOException {
		
		// 서비스의 수정 메서드 호출
		bs.update(boardDTO, uploadPath, file);
		
		// 수정 완료 후 상세 페이지로 리다이렉트
		return "redirect:/board/read/" + boardDTO.getBoardId();
	}
	
	
	@PostMapping("/delete")
	public String delete(@RequestParam Long boardId) {
		
		bs.delete(boardId);
		
		return "redirect:/board/list";
	}
	
	// 1. 피드 수정 페이지 이동 (GET)
	@GetMapping("/feedUpdate/{boardId}")
	public String feedUpdateForm(@PathVariable Long boardId, Model model) {
		// 기존 조회 메서드 재사용 (조회수 증가 없는 findById 사용)
		BoardDTO boardDTO = bs.findById(boardId);
		model.addAttribute("board", boardDTO);
		
		return "feedUpdate"; // feedUpdate.html 로 이동
	}
	
	@PostMapping("/feedUpdate")
	public String feedUpdate(@ModelAttribute BoardDTO boardDTO,
							 @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
		
		// 기존 서비스의 update 메서드 재사용
		bs.update(boardDTO, uploadPath, file);
		
		// 수정 후 '피드 목록'으로 리다이렉트
		return "redirect:/board/feedView";
	}
}
