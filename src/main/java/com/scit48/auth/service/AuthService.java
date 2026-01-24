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
import com.scit48.common.file.FileStorageService;;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    // ===============================
    // 회원가입
    // ===============================
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

        if (image != null && !image.isEmpty()) {
            String savedName = fileStorageService.save(image);
            member.setProfileImage(savedName, "/images/" + savedName);
        }

        memberRepository.save(member);
    }

    // ===============================
    // 로그인
    // ===============================
    public JwtToken login(LoginRequestDto request) {

        MemberEntity member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("ID 또는 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("ID 또는 비밀번호가 틀렸습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(
                member.getId(),
                member.getRole());

        String refreshToken = jwtProvider.createRefreshToken(member.getId());
        refreshTokenRepository.save(member.getId(), refreshToken);

        return new JwtToken(accessToken, refreshToken);
    }

    // ===============================
    // 토큰 재발급
    // ===============================
    public JwtToken reissue(String refreshToken) {

        // 1. 토큰 자체 검증
        if (!jwtProvider.validate(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        Claims claims = jwtProvider.parseClaims(refreshToken);

        // 2. Refresh Token인지 확인
        if (!"REFRESH".equals(claims.get("type"))) {
            throw new IllegalArgumentException("Refresh Token이 아닙니다.");
        }

        Long memberId = Long.valueOf(claims.getSubject());

        // 3. Redis에 저장된 토큰과 비교
        refreshTokenRepository.validate(memberId, refreshToken);

        // 4. 새 Access Token 발급
        String newAccessToken = jwtProvider.createAccessToken(
                memberId,
                "ROLE_MEMBER");

        return new JwtToken(newAccessToken, refreshToken);
    }
}
