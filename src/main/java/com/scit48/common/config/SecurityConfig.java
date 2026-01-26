package com.scit48.common.config;

import com.scit48.auth.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				// CSRF 비활성화 (JWT 사용)
				.csrf(AbstractHttpConfigurer::disable)
				
				// JWT → 세션 사용 안 함
				.sessionManagement(sm ->
						sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				
				// 접근 권한
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/ai/**").permitAll()
						.requestMatchers("/api/**").permitAll()
						.anyRequest().permitAll()
				)
				
				// JWT 필터 등록
				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class
				);
		
		return http.build();
	}
}
