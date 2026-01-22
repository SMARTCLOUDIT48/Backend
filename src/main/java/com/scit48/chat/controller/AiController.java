package com.scit48.chat.controller;

import com.scit48.chat.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files; // ✨ 추가됨
import java.nio.file.Path;  // ✨ 추가됨
import java.nio.file.Paths; // ✨ 추가됨
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
	
	private final AiService aiService;
	
	// 파일 저장 경로 (FileController와 동일하게 맞춤)
	// 윈도우 사용자라면 경로가 맞는지 확인해주세요 (C:/scit_chat/upload/)
	private final String UPLOAD_DIR = "C:/scit_chat/upload/";
	
	// 1. 문법 검사 (기존)
	@PostMapping("/grammar")
	public Map<String, Object> checkGrammar(@RequestBody Map<String, String> request) {
		return aiService.checkGrammar(request.get("message"));
	}
	
	// 2. 텍스트 번역 (기존)
	@PostMapping("/translate")
	public Map<String, String> translateMessage(@RequestBody Map<String, String> request) {
		String translated = aiService.translate(request.get("message"));
		return Map.of("translated", translated);
	}
	
	// ✨ 3. [수정완료] 음성 -> 텍스트 -> 번역 (파일 소실 방지 버전)
	@PostMapping("/voice-send")
	public Map<String, String> voiceToTextAndTranslate(@RequestParam("file") MultipartFile file) throws IOException {
		
		// (1) 폴더 확인 및 생성
		File folder = new File(UPLOAD_DIR);
		if (!folder.exists()) folder.mkdirs();
		
		// 파일명 생성
		String saveName = UUID.randomUUID().toString() + ".webm";
		Path savePath = Paths.get(UPLOAD_DIR + saveName);
		
		// 🚨 [핵심 수정] transferTo 대신 Files.write 사용
		// transferTo는 파일을 이동시켜버려서 AI가 읽을 때 파일이 없지만,
		// 이 방식은 데이터를 복사해서 저장하므로 AI한테 넘겨줄 데이터가 살아있습니다.
		byte[] fileData = file.getBytes();
		Files.write(savePath, fileData);
		
		String fileUrl = "/files/" + saveName;
		
		// (2) AI에게 받아쓰기 시킴 (STT)
		// 위에서 파일 데이터를 메모리에 가지고 있으므로 안전하게 전달 가능
		String sttText = aiService.stt(file);
		log.info("받아쓰기 결과: {}", sttText);
		
		// (3) 받아쓴 글자를 번역 시킴 (Translation)
		String translatedText = aiService.translate(sttText);
		log.info("번역 결과: {}", translatedText);
		
		// (4) 결과 리턴
		Map<String, String> response = new HashMap<>();
		response.put("audioUrl", fileUrl);       // 듣기용 주소
		response.put("text", sttText);           // 받아쓴 원문 (한국어)
		response.put("translated", translatedText); // 번역된 글 (일본어)
		
		return response;
	}
	
	// 3. 호감도 분석 요청
	@PostMapping("/sentiment")
	public Map<String, Object> analyzeSentiment(@RequestBody Map<String, String> request) {
		String chatHistory = request.get("chatHistory");
		log.info("호감도 분석 요청 들어옴");
		return aiService.analyzeSentiment(chatHistory);
	}
	
	// 4. 보내기 전 호감도 체크
	@PostMapping("/pre-check")
	public Map<String, Object> preCheckMessage(@RequestBody Map<String, String> request) {
		return aiService.analyzeMessage(request.get("message"));
	}
}