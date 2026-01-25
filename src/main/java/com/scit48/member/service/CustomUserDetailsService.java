package com.scit48.member.service;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String memberId)
            throws UsernameNotFoundException {

        UserEntity memberEntity = memberRepository.findById(Long.valueOf(memberId))
                .orElseThrow(() -> new UsernameNotFoundException("회원 없음"));

        return new User(
                memberEntity.getId().toString(),
                memberEntity.getPassword(),
                List.of(new SimpleGrantedAuthority(memberEntity.getRole())));
    }
}
