package com.scit48.auth.service;

import com.scit48.auth.dto.LoginRequestDto;
import com.scit48.auth.dto.SignupRequestDto;
import com.scit48.auth.jwt.JwtProvider;
import com.scit48.auth.jwt.JwtToken;
import com.scit48.auth.repository.RefreshTokenRepository;
import com.scit48.member.entity.MemberEntity;
import com.scit48.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor

public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequestDto request, MultipartFile image) {

        if (memberRepository.findByMemberId(request.getMemberId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }

        MemberEntity member = new MemberEntity(
                request.getMemberId(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname(),
                request.getGender(),
                request.getAge(),
                request.getNation(),
                request.getNativeLanguage(),
                request.getLevelLanguage());

        memberRepository.save(member);
    }

    public JwtToken login(LoginRequestDto request) {

        MemberEntity member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("ID 또는 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("ID 또는 비밀번호가 틀렸습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());
        refreshTokenRepository.save(member.getId(), refreshToken);

        return new JwtToken(accessToken, refreshToken);
    }

    public JwtToken reissue(String refreshToken) {
        Claims claims = jwtProvider.parseClaims(refreshToken);

        if (!"REFRESH".equals(claims.get("type"))) {
            throw new IllegalArgumentException("Refresh Token이 아닙니다.");
        }

        Long memberId = Long.valueOf(claims.getSubject());
        refreshTokenRepository.validate(memberId, refreshToken);

        String newAccessToken = jwtProvider.createAccessToken(memberId, "ROLE_MEMBER");
        return new JwtToken(newAccessToken, refreshToken);
    }
}
