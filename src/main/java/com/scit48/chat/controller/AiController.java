package com.scit48.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
	
	// 1. 문법 검사 (Grammar Check)
	@PostMapping("/grammar")
	public Map<String, Object> checkGrammar(@RequestBody Map<String, String> request) {
		String message = request.get("message");
		Map<String, Object> response = new HashMap<>();
		
		if (message == null || message.trim().isEmpty()) {
			response.put("result", "입력 없음");
			return response;
		}
		
		// [가짜 로직] 문법 체크 시뮬레이션
		if (message.contains("I is") || message.contains("She have")) {
			response.put("valid", false);
			response.put("advice", "❌ 문법 오류 발견: 'be동사'나 '수일치'를 확인해보세요.");
		} else {
			response.put("valid", true);
			response.put("advice", "✅ 문법이 완벽합니다!");
		}
		return response;
	}
	
	// 2. 호감도/톤 검사 (Tone Check)
	@PostMapping("/tone")
	public Map<String, Object> checkTone(@RequestBody Map<String, String> request) {
		String message = request.get("message");
		Map<String, Object> response = new HashMap<>();
		
		// [가짜 로직] 단어에 따른 상대방 반응 예측
		if (message.contains("stupid") || message.contains("bad") || message.contains("hate")) {
			response.put("mood", "BAD");
			response.put("advice", "😰 상대방이 상처받을 수 있어요. 조금 더 부드럽게 말해볼까요?");
			response.put("score", 20);
		} else if (message.contains("love") || message.contains("thanks") || message.contains("good")) {
			response.put("mood", "GOOD");
			response.put("advice", "🥰 상대방이 아주 좋아할 말투입니다! 호감도 상승 예정!");
			response.put("score", 95);
		} else {
			response.put("mood", "NEUTRAL");
			response.put("advice", "😐 무난하고 사무적인 말투입니다.");
			response.put("score", 50);
		}
		return response;
	}
}