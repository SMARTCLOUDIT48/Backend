package com.scit48.common.config; // 👈 common 패키지 확인

import com.scit48.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				// CSRF 비활성화 (JWT + Form 혼용해도 문제 없음)
				.csrf(csrf -> csrf.disable())

				// 세션 사용 안 함 (JWT)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 요청 권한 설정
				.authorizeHttpRequests(auth -> auth

						// ===== 페이지 URL (Controller 경유) =====
						.requestMatchers(
								"/",
								"/signup",
								"/login")
						.permitAll()

						// ===== 정적 리소스 =====
						.requestMatchers(
								"/css/**",
								"/js/**",
								"/images/**",
								"/favicon.ico")
						.permitAll()

						// ===== 인증 관련 API =====
						.requestMatchers(
								"/auth/**")
						.permitAll()

						// ===== 그 외 =====
						.anyRequest().authenticated())

				// JWT 필터 등록
				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
