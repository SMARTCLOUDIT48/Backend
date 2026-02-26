package com.scit48.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Value("${file.upload.profile-dir}")
	private String profileUploadDir;
	
	@Value("${file.upload.inquiry-dir}")
	private String inquiryUploadDir;
	
	@Value("${board.uploadPath}")
	private String boardUploadPath;
	
	// 🔥 추가: application.properties의 chat.upload-path 값을 가져옵니다.
	@Value("${chat.upload-path}")
	private String chatUploadPath;
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		
		// 1. 문의 이미지
		registry.addResourceHandler("/images/inquiry/upload/**")
				.addResourceLocations("file:///" + inquiryUploadDir + "/");
		
		// 2. 업로드된 프로필 이미지
		registry.addResourceHandler("/images/profile/upload/**")
				.addResourceLocations("file:///" + profileUploadDir + "/");
		
		// 3. 기본 static 이미지
		registry.addResourceHandler("/images/**")
				.addResourceLocations("classpath:/static/images/");
		
		// 4. 게시글 첨부파일
		registry.addResourceHandler("/files/**")
				.addResourceLocations("file:///" + boardUploadPath + "/");
		
		/*
		 * ===============================
		 * 🎤 5. 채팅 음성 파일 매핑 (최종 수정)
		 * ===============================
		 * properties에 정의된 chat.upload-path를 직접 사용합니다.
		 */
		registry.addResourceHandler("/chat-files/**")
				.addResourceLocations("file:///" + chatUploadPath + "/");
		
	}
}