package com.scit48.Inquiry.controller;

import com.scit48.Inquiry.domain.dto.InquiryAdminListDto;
import com.scit48.Inquiry.domain.dto.InquiryAnswerRequestDto;
import com.scit48.Inquiry.service.AdminInquiryService;
import com.scit48.auth.member.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/inquiries")
public class AdminInquiryController {
	private final AdminInquiryService adminInquiryService;
	
	@GetMapping
	public String inquiryList(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "ALL") String type,
			Model model
	) {
		Page<InquiryAdminListDto> inquiryPage =
				adminInquiryService.getInquiryPage(page, keyword, type);
		
		model.addAttribute("inquiries", inquiryPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", inquiryPage.getTotalPages());
		model.addAttribute("keyword", keyword);
		model.addAttribute("type", type);
		
		return "admin/inquiry-list";
	}
	
	
	
	@PostMapping("/{id}/answer")
	@ResponseBody
	public void answerInquiry(
			@PathVariable Long id,
			@RequestBody InquiryAnswerRequestDto dto,
			@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		Long adminId = userDetails.getUser().getId();
		adminInquiryService.answerInquiry(id, adminId, dto.getContent());
	}
	
	@PutMapping("/{id}/answer")
	@ResponseBody
	public void updateAnswer(
			@PathVariable Long id,
			@RequestBody InquiryAnswerRequestDto dto,
			@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		Long adminId = userDetails.getUser().getId();
		adminInquiryService.updateAnswer(id, adminId, dto.getContent());
	}
	
	
}
