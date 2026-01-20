package com.scit48.chat.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {
	
	// 저장할 로컬 경로 (없으면 폴더를 직접 만들어주세요!)
	private final String UPLOAD_DIR = "C:/scit_chat/upload/";
	
	@PostMapping("/upload")
	public Map<String, String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
		// 1. 폴더 확인 및 생성
		File folder = new File(UPLOAD_DIR);
		if (!folder.exists()) folder.mkdirs();
		
		// 2. 파일명 중복 방지 (UUID 사용)
		String originalName = file.getOriginalFilename();
		String saveName = UUID.randomUUID().toString() + "_" + originalName;
		
		// 3. 파일 저장
		file.transferTo(new File(UPLOAD_DIR + saveName));
		
		// 4. 접근 가능한 URL 리턴
		Map<String, String> response = new HashMap<>();
		// WebMvcConfig에서 설정한 경로 (/files/...)
		response.put("url", "/files/" + saveName);
		
		return response;
	}
}