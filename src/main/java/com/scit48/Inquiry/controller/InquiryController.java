package com.scit48.Inquiry.controller;

import com.scit48.Inquiry.domain.entity.InquiryEntity;
import com.scit48.Inquiry.domain.entity.InquiryType;
import com.scit48.Inquiry.file.InquiryFileStorageService;
import com.scit48.Inquiry.service.InquiryService;
import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.Inquiry.file.InquiryFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/inquiry")
public class InquiryController {
	
	private final InquiryService inquiryService;
	private final InquiryFileStorageService inquiryFileStorageService;
	
	@GetMapping
	public String inquiryPage(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			Model model
	) {
		// 🔒 로그인 안 했으면 접근 불가
		if (userDetails == null) {
			return "redirect:/login";
		}
		
		Long userId = userDetails.getUser().getId();
		
		model.addAttribute(
				"inquiries",
				inquiryService.findMyInquiries(userId)
		);
		
		return "Inquiry/inquiry";
	}
	
	@PostMapping
	public String submitInquiry(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam InquiryType type,
			@RequestParam String title,
			@RequestParam String content,
			@RequestParam(required = false) MultipartFile image
	) {
		if (userDetails == null) {
			return "redirect:/login";
		}
		
		Long userId = userDetails.getUser().getId();
		
		String attachmentName = null;
		String attachmentPath = null;
		
		if (image != null && !image.isEmpty()) {
			attachmentName = image.getOriginalFilename();
			attachmentPath = inquiryFileStorageService.saveInquiryImage(image);
		}
		
		inquiryService.create(
				userId, type, title, content, attachmentName, attachmentPath
		);
		
		return "redirect:/customer/inquiry";
	}
	
	@GetMapping("/{id}")
	public String inquiryDetail(
			@PathVariable Long id,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			Model model
	) {
		Long userId = userDetails.getUser().getId();
		
		InquiryEntity inquiry = inquiryService.findMyInquiry(id, userId);
		model.addAttribute("inquiry", inquiry);
		
		return "Inquiry/inquiry-detail";
	}
	
	@PostMapping("/{id}/edit")
	public String editInquiry(
			@PathVariable Long id,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam String title,
			@RequestParam String content,
			@RequestParam(required = false) MultipartFile image
	) {
		Long userId = userDetails.getUser().getId();
		
		String attachmentName = null;
		String attachmentPath = null;
		
		if (image != null && !image.isEmpty()) {
			attachmentName = image.getOriginalFilename();
			attachmentPath = inquiryFileStorageService.saveInquiryImage(image);
		}
		
		inquiryService.update(id, userId, title, content, attachmentName, attachmentPath);
		
		return "redirect:/customer/inquiry/" + id;
	}
	
	@PostMapping("/{id}/delete")
	public String deleteInquiry(
			@PathVariable Long id,
			@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		Long userId = userDetails.getUser().getId();
		inquiryService.delete(id, userId);
		return "redirect:/customer/inquiry";
	}
	
}

