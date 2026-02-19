package com.scit48.admin.controller;

import com.scit48.admin.dto.AdminUserListDTO;
import com.scit48.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
	
	private final AdminUserService adminUserService;
	
	@GetMapping
	public String userList(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String nation,
			@RequestParam(defaultValue = "0") int page,
			Model model
	) {
		Page<AdminUserListDTO> result =
				adminUserService.getUserList(keyword, nation, page);
		
		model.addAttribute("users", result.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", result.getTotalPages());
		
		return "admin/user/user-list";
	}
	
	
	@GetMapping("/{userId}")
	public String userDetail(
			@PathVariable Long userId,
			Model model
	) {
		model.addAttribute(
				"user",
				adminUserService.getUserDetail(userId)
		);
		return "admin/user/user-detail";
	}
	
	@PostMapping("/{userId}/delete")
	@ResponseBody
	public void deleteUser(@PathVariable Long userId) {
		adminUserService.deleteUser(userId);
	}
}
