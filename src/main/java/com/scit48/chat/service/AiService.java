package com.scit48.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {
	
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	
	@Value("${google.ai.key}")
	private String apiKey;
	
	@Value("${google.ai.url}")
	private String chatUrl; // 채팅용 (Llama)
	
	// STT용 주소 (Groq Whisper)
	private final String STT_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
	
	// 1. 문법 교정 (기존)
	public Map<String, Object> checkGrammar(String message) {
		String prompt = """
                Check grammar for the following sentence.
                Return ONLY JSON format.
                JSON Schema: { "corrected": "...", "explanation_kr": "...", "explanation_jp": "..." }
                Input: """ + message;
		
		// JSON 모드 켜기 (true)
		String jsonResponse = callGroqChatApi(prompt, true);
		try {
			return objectMapper.readValue(jsonResponse, Map.class);
		} catch (JsonProcessingException e) {
			return Map.of("error", "Parsing Error");
		}
	}
	
	// 2. 스마트 번역 (기존)
	public String translate(String message) {
		String prompt = String.format(
				"Translate smoothly.\n" +
						"Rule: Korean -> Japanese, Japanese -> Korean.\n" +
						"Return ONLY the translated text.\nText: %s", message
		);
		// JSON 모드 끄기 (false) -> 텍스트만 받음
		return callGroqChatApi(prompt, false);
	}
	
	// 3. 음성 -> 텍스트 변환 (STT)
	public String stt(MultipartFile file) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(apiKey);
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			
			ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
				@Override
				public String getFilename() {
					return "audio.webm";
				}
			};
			
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", fileResource);
			body.add("model", "whisper-large-v3");
			body.add("response_format", "json");
			
			HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
			
			ResponseEntity<String> response = restTemplate.postForEntity(STT_URL, entity, String.class);
			
			JsonNode root = objectMapper.readTree(response.getBody());
			return root.path("text").asText().trim();
			
		} catch (Exception e) {
			log.error("STT 상세 에러: ", e);
			return "오류 발생: " + e.getMessage();
		}
	}
	
	// 💘 4. [수정됨] 호감도 분석 (Strict Mode)
	public Map<String, Object> analyzeSentiment(String chatHistory) {
		String prompt = """
            Role: You are a sharp-witted 'Dating Coach'.
            Task: Analyze the dialogue and provide feedback in JSON format.
            
            [STRICT Language Rules]
            1. First, identify the DOMINANT language of the dialogue.
            2. IF Korean (한국어):
               - Output MUST be 100% Korean.
               - Do NOT use Chinese characters (e.g., '関係', '是什么').
               - Do NOT use English.
            3. IF Japanese (日本語):
               - Output MUST be 100% Japanese.
            
            [JSON Output Schema]
            {
              "score": (Integer 0-100),
              "comment": "One-line assessment (Keep it witty and cynical)",
              "advice": "Actionable advice for the user"
            }

            [Dialogue to Analyze]
            """ + chatHistory;
		
		// JSON 모드로 호출
		String jsonResponse = callGroqChatApi(prompt, true);
		
		try {
			return objectMapper.readValue(jsonResponse, Map.class);
		} catch (Exception e) {
			log.error("JSON 파싱 오류", e);
			return Map.of("score", 0, "comment", "분석 실패 (Analysis Failed)", "advice", "Try again.");
		}
	}
	
	// ✨ 5. [수정됨] 멘트 체크 (Strict JSON Mode)
	public Map<String, Object> analyzeMessage(String message) {
		String prompt = """
            Role: You are a 'Dating Consultant' checking a user's draft message.
            Task: Evaluate the message and suggest a better version in JSON.

            [STRICT Language Rules]
            1. Identify the language of the [User's Draft Message].
            2. IF Korean:
               - 'feedback' and 'better_version' MUST be in natural Korean.
               - NEVER use Chinese characters.
            3. IF Japanese:
               - Output MUST be in Japanese.

            [JSON Output Schema] - USE THESE EXACT KEYS:
            {
              "score": (Integer 0-100),
              "risk": "Safe" or "Caution" or "Danger",
              "feedback": "1-2 sentences explaining why this score was given",
              "better_version": "A revised, more attractive message (keep it empty string if perfect)"
            }

            [User's Draft Message]
            """ + message;
		
		try {
			// Groq API 호출
			String jsonResponse = callGroqChatApi(prompt, true);
			
			// JSON 파싱
			Map<String, Object> result = objectMapper.readValue(jsonResponse, Map.class);
			
			// 🚨 안전장치: 혹시 AI가 키를 빼먹었을 경우를 대비한 기본값 설정
			result.putIfAbsent("score", 50);
			result.putIfAbsent("risk", "Caution");
			result.putIfAbsent("feedback", "AI가 피드백을 생성하지 못했습니다.");
			result.putIfAbsent("better_version", "");
			
			return result;
			
		} catch (Exception e) {
			log.error("AI Analysis Error: ", e);
			// 에러 발생 시 클라이언트가 멈추지 않도록 기본값 반환
			return Map.of(
					"score", 0,
					"risk", "Error",
					"feedback", "분석 중 오류가 발생했습니다. 다시 시도해주세요.",
					"better_version", ""
			);
		}
	}
	
	// (공통 메서드) Groq API 호출 로직
	private String callGroqChatApi(String prompt, boolean isJsonMode) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(apiKey);
		
		Map<String, Object> body = new HashMap<>();
		body.put("model", "llama-3.3-70b-versatile"); // 모델명 확인
		
		List<Map<String, String>> messages = new ArrayList<>();
		Map<String, String> userMessage = new HashMap<>();
		userMessage.put("role", "user");
		userMessage.put("content", prompt);
		messages.add(userMessage);
		body.put("messages", messages);
		
		// JSON 모드일 때만 포맷 지정
		if (isJsonMode) {
			body.put("response_format", Map.of("type", "json_object"));
		}
		
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(chatUrl, entity, Map.class);
			Map<String, Object> firstChoice = (Map) ((List) response.getBody().get("choices")).get(0);
			return (String) ((Map) firstChoice.get("message")).get("content");
		} catch (Exception e) {
			log.error("API 호출 중 오류 발생", e);
			return "Error";
		}
	}
}