package com.scit48.auth.member.service; // ✅ 패키지 경로 확인

import com.scit48.common.domain.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

// 🟢 이게 있어야 컨트롤러에서 에러가 안 납니다!
public class CustomUserDetails implements UserDetails {
	
	private final UserEntity user;
	
	public CustomUserDetails(UserEntity user) {
		this.user = user;
	}
	
	// ⭐ 컨트롤러에서 .getUser()를 호출하기 위해 필요한 핵심 메서드
	public UserEntity getUser() {
		return user;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(user.getRole()));
	}
	
	@Override
	public String getPassword() { return user.getPassword(); }
	@Override
	public String getUsername() { return user.getMemberId(); }
	@Override
	public boolean isAccountNonExpired() { return true; }
	@Override
	public boolean isAccountNonLocked() { return true; }
	@Override
	public boolean isCredentialsNonExpired() { return true; }
	@Override
	public boolean isEnabled() { return true; }
}