package com.scit48;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// [핵심] scanBasePackages를 지정하면, AppConfig(상위)와 SecurityConfig(옆집)를 모두 읽습니다.
@SpringBootApplication(scanBasePackages = "com.scit48")
@EnableJpaAuditing
public class ChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatApplication.class, args);
	}

}
