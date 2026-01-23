package com.scit48.member.service;

import com.scit48.member.entity.MemberEntity;
import com.scit48.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security용 UserDetailsService
 * - JWT 필터에서 호출됨
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * memberId(String) 기반 UserDetails 조회
     */
    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {

        MemberEntity memberEntity = memberRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new UsernameNotFoundException("회원 없음"));

        return new User(
                memberEntity.getMemberId().toString(),
                memberEntity.getPassword(),
                List.of(new SimpleGrantedAuthority(memberEntity.getRole())));
    }
}
