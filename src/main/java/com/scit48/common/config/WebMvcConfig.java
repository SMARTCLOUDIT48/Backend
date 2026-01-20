package com.scit48.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	// 브라우저에서 /files/** 로 접근하면 로컬 폴더 C:/scit_chat/upload/ 에서 파일을 찾음
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/files/**")
				.addResourceLocations("file:///C:/scit_chat/upload/");
	}
}