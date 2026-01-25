package com.scit48.auth.service;

import com.scit48.auth.dto.LoginRequestDto;
import com.scit48.auth.dto.SignupRequestDto;
import com.scit48.auth.jwt.JwtProvider;
import com.scit48.auth.jwt.JwtToken;
import com.scit48.auth.repository.RefreshTokenRepository;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.exception.BadRequestException;
import com.scit48.common.exception.UnauthorizedException;
import com.scit48.common.file.FileStorageService;
import com.scit48.common.repository.UserRepository;
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
    private final UserRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    // ===============================
    // 회원가입
    // ===============================
    public UserEntity signup(SignupRequestDto request, MultipartFile image) {

        if (memberRepository.existsByMemberId(request.getMemberId())) {
            throw new BadRequestException("이미 존재하는 ID입니다.");
        }

        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new BadRequestException("이미 사용 중인 닉네임입니다.");
        }

        UserEntity member = UserEntity.builder()
                .memberId(request.getMemberId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .gender(request.getGender())
                .age(request.getAge())
                .nation(request.getNation())
                .nativeLanguage(request.getNativeLanguage())
                .levelLanguage(request.getLevelLanguage())
                .role("ROLE_MEMBER")
                .build();

        if (image != null && !image.isEmpty()) {
            String savedName = fileStorageService.save(image);
            member.updateProfileImage(savedName, "/images/" + savedName);
        }

        return memberRepository.save(member);
    }

    // ===============================
    // 로그인
    // ===============================
    public JwtToken login(LoginRequestDto request) {

        UserEntity member = memberRepository.findByMemberId(request.getMemberId())
                .orElseThrow(() -> new UnauthorizedException("ID 또는 비밀번호가 틀렸습니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new UnauthorizedException("ID 또는 비밀번호가 틀렸습니다.");
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

        if (!jwtProvider.validate(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }

        Claims claims = jwtProvider.parseClaims(refreshToken);

        if (!"REFRESH".equals(claims.get("type"))) {
            throw new BadRequestException("Refresh Token이 아닙니다.");
        }

        Long memberId = Long.valueOf(claims.getSubject());

        refreshTokenRepository.validate(memberId, refreshToken);

        UserEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UnauthorizedException("회원이 존재하지 않습니다."));

        String newAccessToken = jwtProvider.createAccessToken(
                memberId,
                member.getRole());

        return new JwtToken(newAccessToken, refreshToken);
    }
}
