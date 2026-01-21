package com.scit48.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
	
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	
	// secrets.properties에 저장한 Groq 키와 주소를 가져옵니다
	@Value("${google.ai.key}")
	private String apiKey;
	
	@Value("${google.ai.url}")
	private String url;
	
	@PostMapping("/grammar")
	public Map<String, Object> checkGrammar(@RequestBody Map<String, String> request) {
		String message = request.get("message");
		log.info("문법 검사 요청: {}", message);
		
		// 1. 프롬프트(명령어) 구성 - 여기가 핵심 수정 부분입니다!
		// 한국어(explanation_kr)와 일본어(explanation_jp) 설명을 모두 달라고 요청합니다.
		String prompt = """
                Check grammar for the following sentence.
                Return ONLY JSON format. Do not include any other text.
                
                JSON Schema:
                {
                    "corrected": "Corrected English sentence",
                    "explanation_kr": "Explanation in Korean",
                    "explanation_jp": "Explanation in Japanese"
                }
                
                Input sentence: """ + message;
		
		// 2. 요청 헤더 설정 (인증 키)
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(apiKey);
		
		// 3. 요청 바디 설정
		Map<String, Object> body = new HashMap<>();
		body.put("model", "llama-3.3-70b-versatile"); // Groq 고성능 모델
		
		// 메시지 리스트 구성
		List<Map<String, String>> messages = new ArrayList<>();
		Map<String, String> userMessage = new HashMap<>();
		userMessage.put("role", "user");
		userMessage.put("content", prompt); // 위에서 만든 상세 프롬프트를 넣습니다.
		messages.add(userMessage);
		
		body.put("messages", messages);
		body.put("response_format", Map.of("type", "json_object")); // JSON 응답 강제
		
		// 4. 전송 및 결과 받기
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
		
		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
			
			// 응답 파싱
			Map<String, Object> responseBody = response.getBody();
			List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
			Map<String, Object> firstChoice = choices.get(0);
			Map<String, Object> messageContent = (Map<String, Object>) firstChoice.get("message");
			String contentString = (String) messageContent.get("content");
			
			// AI가 준 문자열을 다시 JSON 객체로 변환해서 리턴
			return objectMapper.readValue(contentString, Map.class);
			
		} catch (Exception e) {
			log.error("Groq API 통신 중 오류 발생", e);
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("error", "AI 서버 연결 실패: " + e.getMessage());
			return errorResponse;
		}
	}
}