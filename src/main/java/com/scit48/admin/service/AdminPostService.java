package com.scit48.admin.service;

import com.scit48.admin.dto.AdminPostListDTO;
import com.scit48.community.repository.BoardRepository;
import com.scit48.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPostService {
	private final BoardRepository boardRepository;
	private final NoticeRepository noticeRepository;
	
	private static final int PAGE_SIZE = 10;
	
	public Page<AdminPostListDTO> getPosts(
			String board,
			String searchType,
			String keyword,
			int page
	) {
		Pageable pageable = PageRequest.of(page, PAGE_SIZE);
		
		if ("COMMUNITY".equals(board)) {
			return boardRepository.findAdminBoardPosts(searchType, keyword, pageable);
		}
		
		if ("NOTICE".equals(board) || "FAQ".equals(board)) {
			return noticeRepository.findAdminNoticePosts(board, searchType, keyword, pageable);
		}
		
		// ===== ALL =====
		int need = (page + 1) * PAGE_SIZE;
		List<AdminPostListDTO> merged = new ArrayList<>();
		
		merged.addAll(
				boardRepository.findAdminBoardPosts(
						searchType, keyword, PageRequest.of(0, need)
				).getContent()
		);
		
		merged.addAll(
				noticeRepository.findAdminNoticePosts(
						null, searchType, keyword, PageRequest.of(0, need)
				).getContent()
		);
		
		merged.sort(Comparator.comparing(AdminPostListDTO::getCreatedAt).reversed());
		
		int from = page * PAGE_SIZE;
		int to = Math.min(from + PAGE_SIZE, merged.size());
		
		List<AdminPostListDTO> content =
				from >= merged.size() ? Collections.emptyList() : merged.subList(from, to);
		
		long total =
				boardRepository.findAdminBoardPosts(searchType, keyword, Pageable.unpaged()).getTotalElements()
						+ noticeRepository.findAdminNoticePosts(null, searchType, keyword, Pageable.unpaged()).getTotalElements();
		
		return new PageImpl<>(content, pageable, total);
	}
	
	public void deletePost(String board, Long id) {
		if ("COMMUNITY".equals(board)) {
			boardRepository.deleteById(id);
		} else {
			noticeRepository.deleteById(id);
		}
	}
}
