package com.scit48.common.config;

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
				// CSRF 비활성화
				.csrf(csrf -> csrf.disable())

				// JWT → 세션 미사용
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth

						// =====================
						// 페이지 (Controller)
						// =====================
						.requestMatchers(
								"/",
								"/home/**",
								"/login",
								"/signup")
						.permitAll()

						// =====================
						// 정적 리소스
						// =====================
						.requestMatchers(
								"/css/**",
								"/js/**",
								"/images/**",
								"/home/css/**",
								"/home/js/**",
								"/home/images/**",
								"/favicon.ico")
						.permitAll()

						// =====================
						// 인증 API
						// =====================
						.requestMatchers(
								"/auth/login",
								"/auth/signup",
								"/auth/check-member-id")
						.permitAll()

						.requestMatchers("/auth/reissue")
						.authenticated()

						// =====================
						// 나머지는 인증 필요
						// =====================
						.anyRequest().authenticated())

				// JWT 필터
				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
