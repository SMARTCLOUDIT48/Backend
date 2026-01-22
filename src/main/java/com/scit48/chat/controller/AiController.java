package com.scit48.chat.controller;

import com.scit48.chat.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
	
	private final AiService aiService; // 이제 Service를 부릅니다!
	
	// 1. 문법 검사
	@PostMapping("/grammar")
	public Map<String, Object> checkGrammar(@RequestBody Map<String, String> request) {
		String message = request.get("message");
		log.info("문법 검사 요청: {}", message);
		
		return aiService.checkGrammar(message);
	}
	
	// 2. 실시간 번역
	@PostMapping("/translate")
	public Map<String, String> translateMessage(@RequestBody Map<String, String> request) {
		String originalMessage = request.get("message");
		String targetLang = request.get("targetLang"); // "KO", "JA", "EN"
		
		// 서비스 호출
		String translatedText = aiService.translate(originalMessage, targetLang);
		
		// 결과 포장
		Map<String, String> response = new HashMap<>();
		response.put("original", originalMessage);
		response.put("translated", translatedText);
		
		return response;
	}
}