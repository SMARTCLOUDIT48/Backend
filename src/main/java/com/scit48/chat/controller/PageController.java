package com.scit48.chat.controller; // ✅ 보여주신 패키지명과 동일하게 맞췄습니다.

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
	
	/**
	 * 브라우저 주소창에 "localhost:8080/chat" 입력 시 실행
	 * templates/chat.html 파일을 찾아서 보여줌
	 */
/**@GetMapping("/chat")
	public String showChatPage() {
		return "chat"; // chat.html의 .html을 뺀 이름
	}
 */
}