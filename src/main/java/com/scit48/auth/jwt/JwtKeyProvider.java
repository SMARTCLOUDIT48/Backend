package com.scit48.auth.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * JWT 서명에 사용할 Secret Key 제공 클래스
 * - (수정) 설정 파일 로딩 에러 방지를 위해 키 값 직접 입력
 */
@Component
public class JwtKeyProvider {
	
	private final Key key;
	
	public JwtKeyProvider() {
		// ✅ 보내주신 키 값을 여기에 직접 넣었습니다.
		String secretKey = "ZmFzdHNlY3VyZS1qd3Qtc2VjcmV0LWtleS0zMi1ieXRlcy1sb25nLXN0cmluZw==";
		
		// 이 키는 Base64 형식이므로 디코딩해서 사용해야 합니다. (이 로직이 맞습니다)
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}
	
	public Key getKey() {
		return key;
	}
}