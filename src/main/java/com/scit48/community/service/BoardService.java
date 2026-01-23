package com.scit48.community.service;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.repository.UserRepository;
import com.scit48.community.domain.dto.BoardDTO;
import com.scit48.community.domain.entity.BoardEntity;
import com.scit48.community.domain.entity.CategoryEntity;
import com.scit48.community.repository.BoardRepository;
import com.scit48.community.repository.CategoryRepository;
import com.scit48.community.util.FileManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BoardService {
	
	private final BoardRepository br;
	private final CategoryRepository ctr;
	private final UserRepository ur;
	private final FileManager fm;
	
	@Transactional(rollbackOn = IOException.class)
	public void write(BoardDTO boardDTO, String uploadPath, MultipartFile upload) throws IOException {
		// 1. 작성자(User) 조회
		UserEntity userEntity = ur.findById(boardDTO.getId())
				.orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. id=" + boardDTO.getId()));
		
		// 2. 카테고리 조회 (DTO에서 ID를 받아옴)
		CategoryEntity categoryEntity = ctr.findByName(boardDTO.getCategoryName())
				.orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 없습니다. name=" + boardDTO.getCategoryName()));
		
		
		// 4. Entity 변환 및 저장 (Builder 패턴 사용)
		BoardEntity boardEntity = BoardEntity.builder()
				.title(boardDTO.getTitle())
				.content(boardDTO.getContent())
				.viewCount(0)
				.category(categoryEntity) // 연관관계 설정
				.user(userEntity)         // 연관관계 설정
				.build();
		
		// 첨부파일이 있는 경우
		if (upload != null && !upload.isEmpty()) {
			String fileName = fm.saveFile(uploadPath, upload);
			boardEntity.setFileName(fileName);
			boardEntity.setFileOriginalName(upload.getOriginalFilename());
		}
		
		br.save(boardEntity);
	}
	
	
	public void feedWrite(BoardDTO boardDTO, String uploadPath,
						  MultipartFile upload) throws IOException {
		
		// 1. 작성자(User) 조회
		UserEntity userEntity = ur.findById(boardDTO.getId())
				.orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다. id=" + boardDTO.getId()));
		
		// 2. 카테고리 조회 (DTO에서 ID를 받아옴)
		CategoryEntity categoryEntity = ctr.findByName(boardDTO.getCategoryName())
				.orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 없습니다. name=" + boardDTO.getCategoryName()));
		
		
		// 4. Entity 변환 및 저장 (Builder 패턴 사용)
		BoardEntity boardEntity = BoardEntity.builder()
				.title(boardDTO.getTitle())
				.content(boardDTO.getContent())
				.viewCount(0)
				.category(categoryEntity) // 연관관계 설정
				.user(userEntity)         // 연관관계 설정
				.build();
		
		// 첨부파일이 있는 경우
		if (upload != null && !upload.isEmpty()) {
			String fileName = fm.saveFile(uploadPath, upload);
			boardEntity.setFileName(fileName);
			boardEntity.setFileOriginalName(upload.getOriginalFilename());
		}
		
		br.save(boardEntity);
	}
}
