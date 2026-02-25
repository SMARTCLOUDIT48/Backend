package com.scit48.admin.service;

import com.scit48.admin.dto.AdminUserDetailDTO;
import com.scit48.admin.dto.AdminUserListDTO;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserService {
	private final UserRepository userRepository;
	
	public Page<AdminUserListDTO> getUserList(
			String keyword, String nation, int page
	) {
		
		if (keyword != null && keyword.isBlank()) {
			keyword = null;
		}
		
		if (nation != null && nation.isBlank()) {
			nation = null;
		}
		
		return userRepository.findAdminUsers(
				keyword, nation,
				PageRequest.of(page, 10)
		);
	}
	
	public AdminUserDetailDTO getUserDetail(Long userId) {
		UserEntity u = userRepository.findById(userId)
				.orElseThrow();
		
		if ("ADMIN".equals(u.getRole())) {
			throw new IllegalStateException("관리자 계정은 조회할 수 없습니다.");
		}
		
		String profileImageUrl;
		
		if (u.getProfileImagePath() != null && u.getProfileImageName() != null) {
			profileImageUrl = u.getProfileImagePath() + "/" + u.getProfileImageName();
		} else {
			profileImageUrl = "/images/profile/default.png";
		}
		
		return new AdminUserDetailDTO(
				u.getId(),
				u.getMemberId(),
				u.getNickname(),
				u.getGender().name(),
				u.getAge(),
				u.getNation(),
				u.getIntro(),
				u.getNativeLanguage(),
				u.getLevelLanguage().name(),
				u.getManner(),
				u.getCreatedAt(),
				profileImageUrl   // 🔥 완성된 URL
		);
	}
	
	
	@Transactional
	public void deleteUser(Long userId) {
		userRepository.deleteById(userId);
	}
}
