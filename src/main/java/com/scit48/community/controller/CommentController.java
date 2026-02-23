package com.scit48.community.controller;

import com.scit48.community.domain.dto.CommentDTO;
import com.scit48.community.service.BoardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("comment")
public class CommentController {
	
	private final BoardService bs;
	
	// 댓글 작성 (AJAX)
	@PostMapping("/write")
	@ResponseBody // JSON 반환
	public ResponseEntity<CommentDTO> write(@RequestBody CommentDTO commentDTO,
											@AuthenticationPrincipal UserDetails userDetails) {
		
		// 로그인 체크
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		// 서비스 호출
		CommentDTO createdComment = bs.commentWrite(
				commentDTO.getBoardId(),
				userDetails.getUsername(), // 로그인 ID
				commentDTO.getContent()
		);
		
		return ResponseEntity.ok(createdComment);
	}
	
	// 댓글 수정 (AJAX)
	@PostMapping("/update")
	@ResponseBody
	public ResponseEntity<CommentDTO> updateComment(@RequestBody Map<String, Object> params,
													@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		Long commentId = Long.parseLong(params.get("commentId").toString());
		String content = params.get("content").toString();
		
		CommentDTO updatedComment = bs.updateComment(
				commentId,
				userDetails.getUsername(),
				content
		);
		
		return ResponseEntity.ok(updatedComment);
	}
	
	// 댓글 삭제
	@PostMapping("/delete")
	@ResponseBody
	public ResponseEntity<String> deleteComment(@RequestBody Map<String, Long> params,
												@AuthenticationPrincipal UserDetails userDetails) {
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		Long commentId = params.get("commentId");
		
		try {
			bs.deleteComment(commentId, userDetails.getUsername());
			return ResponseEntity.ok("삭제 성공");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}
	
	// 피드의 댓글 목록 조회
	@GetMapping("/list/{boardId}")
	@ResponseBody
	public ResponseEntity<List<CommentDTO>> getFeedComments(@PathVariable Long boardId) {
		List<CommentDTO> comments = bs.getCommentsByBoardId(boardId);
		return ResponseEntity.ok(comments);
	}
	
	// 피드 댓글 작성
	@PostMapping("/feedWrite")
	@ResponseBody
	public ResponseEntity<CommentDTO> writeComment(@RequestBody Map<String, Object> params,
												   @AuthenticationPrincipal UserDetails userDetails) {
		// 로그인 체크
		if (userDetails == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		Long boardId = Long.parseLong(params.get("boardId").toString());
		String content = params.get("content").toString();
		String memberId = userDetails.getUsername();
		
		CommentDTO savedComment = bs.commentWrite(boardId, memberId, content);
		
		return ResponseEntity.ok(savedComment);
	}
	
}
