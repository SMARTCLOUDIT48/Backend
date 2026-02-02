package com.scit48.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	// application.properties 또는 secrets.properties에 설정
	// 예: file.upload.profile-dir=C:/scit_chat/upload/profile
	@Value("${file.upload.profile-dir}")
	private String profileUploadDir;
	
	@Value("${file.upload.inquiry-dir}")
	private String inquiryUploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		
		// 🔥 1️⃣ 문의 이미지 (가장 구체적인 경로 먼저!)
		registry.addResourceHandler("/images/inquiry/upload/**")
				.addResourceLocations("file:" + inquiryUploadDir + "/");
		

		/*
		 * ===============================
		 * 1. 업로드된 프로필 이미지
		 * ===============================
		 * URL:
		 * /images/profile/upload/xxx.png
		 *
		 * 실제 파일 위치:
		 * C:/scit_chat/upload/profile/xxx.png
		 */
		registry.addResourceHandler("/images/profile/upload/**")
				.addResourceLocations("file:" + profileUploadDir + "/");

		/*
		 * ===============================
		 * 2. 기본 static 이미지 (classpath)
		 * ===============================
		 * URL:
		 * /images/profile/default.png
		 *
		 * 실제 파일 위치:
		 * src/main/resources/static/images/profile/default.png
		 */
		registry.addResourceHandler("/images/**")
				.addResourceLocations("classpath:/static/images/");
		
		
	}
}
