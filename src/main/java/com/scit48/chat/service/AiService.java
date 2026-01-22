package com.scit48.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service // 스프링에게 "이건 핵심 로직을 담당하는 서비스야"라고 알려줌
@RequiredArgsConstructor
public class AiService {
	
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper; // JSON 변환기
	
	@Value("${google.ai.key}")
	private String apiKey;
	
	@Value("${google.ai.url}")
	private String url;
	
	// =================================================================
	// 1. 문법 교정 기능 (JSON 포맷 리턴)
	// =================================================================
	public Map<String, Object> checkGrammar(String message) {
		// 프롬프트 작성
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
		
		// API 호출 (JSON 모드 켜기: true)
		String jsonResponse = callGroqApi(prompt, true);
		
		// 결과 파싱 (String -> Map)
		try {
			return objectMapper.readValue(jsonResponse, Map.class);
		} catch (JsonProcessingException e) {
			log.error("AI 응답 파싱 실패", e);
			return Map.of("error", "AI 응답을 해석할 수 없습니다.");
		}
	}
	
	// =================================================================
	// 2. 실시간 번역 기능 (텍스트 리턴)
	// =================================================================
	public String translate(String message, String targetLangCode) {
		// 타겟 언어 설정
		String targetLanguage = "Korean"; // 기본값
		if ("JA".equalsIgnoreCase(targetLangCode)) targetLanguage = "Japanese";
		else if ("EN".equalsIgnoreCase(targetLangCode)) targetLanguage = "English";
		
		// 프롬프트 작성
		String prompt = String.format(
				"Translate the following text to %s. " +
						"Do not add any explanations or quotes. Just provide the translated text.\n\n" +
						"Text: %s",
				targetLanguage, message
		);
		
		// API 호출 (JSON 모드 끄기: false)
		return callGroqApi(prompt, false);
	}
	
	// =================================================================
	// 🛠️ 내부 공통 메서드: 실제 API 통신 담당
	// =================================================================
	private String callGroqApi(String prompt, boolean isJsonMode) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(apiKey);
		
		Map<String, Object> body = new HashMap<>();
		body.put("model", "llama-3.3-70b-versatile");
		
		// 메시지 구성
		List<Map<String, String>> messages = new ArrayList<>();
		Map<String, String> userMessage = new HashMap<>();
		userMessage.put("role", "user");
		userMessage.put("content", prompt);
		messages.add(userMessage);
		body.put("messages", messages);
		
		// JSON 모드가 필요하면 설정 추가
		if (isJsonMode) {
			body.put("response_format", Map.of("type", "json_object"));
		}
		
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
		
		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
			Map<String, Object> responseBody = response.getBody();
			
			if (responseBody == null) return "Error: Empty Body";
			
			List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
			Map<String, Object> firstChoice = choices.get(0);
			Map<String, Object> messageContent = (Map<String, Object>) firstChoice.get("message");
			
			return (String) messageContent.get("content");
			
		} catch (Exception e) {
			log.error("Groq API 호출 중 오류 발생", e);
			return "Error: " + e.getMessage();
		}
	}
}