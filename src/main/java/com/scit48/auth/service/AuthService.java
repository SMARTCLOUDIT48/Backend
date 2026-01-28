package com.scit48.auth.service;

import com.scit48.auth.dto.LoginRequestDto;
import com.scit48.auth.dto.SignupRequestDto;
import com.scit48.auth.jwt.JwtProvider;
import com.scit48.auth.jwt.JwtToken;
import com.scit48.auth.repository.RefreshTokenRepository;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.enums.LanguageLevel;
import com.scit48.common.exception.BadRequestException;
import com.scit48.common.exception.UnauthorizedException;
import com.scit48.common.file.FileStorageService;
import com.scit48.common.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtProvider jwtProvider;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final FileStorageService fileStorageService;

	// ===============================
	// 회원가입
	// ===============================
	@Transactional
	public UserEntity signup(SignupRequestDto request, MultipartFile image) {

		if (userRepository.existsByMemberId(request.getMemberId())) {
			throw new BadRequestException("이미 존재하는 ID입니다.");
		}

		if (userRepository.existsByNickname(request.getNickname())) {
			throw new BadRequestException("이미 사용 중인 닉네임입니다.");
		}

		// 학습 언어 처리
		String studyLang = request.getStudyLanguage();

		if (studyLang == null || studyLang.isBlank()) {
			throw new BadRequestException("학습 언어를 선택해주세요.");
		}

		studyLang = studyLang.trim().toUpperCase();

		String nativeLang;
		if ("KOREAN".equals(studyLang)) {
			nativeLang = "JAPANESE";
		} else if ("JAPANESE".equals(studyLang)) {
			nativeLang = "KOREAN";
		} else {
			throw new BadRequestException("지원하지 않는 언어입니다.");
		}

		UserEntity user = UserEntity.builder()
				.memberId(request.getMemberId())
				.password(passwordEncoder.encode(request.getPassword()))
				.nickname(request.getNickname())
				.gender(request.getGender())
				.age(request.getAge())
				.nation(request.getNation())
				.studyLanguage(studyLang)
				.nativeLanguage(nativeLang)
				.levelLanguage(request.getLevelLanguage())
				.role("ROLE_MEMBER")
				.profileImageName("default.png")
				.profileImagePath("/images/profile")
				.build();

		if (image != null && !image.isEmpty()) {
			String savedName = fileStorageService.saveProfileImage(image);
			user.updateProfileImage(savedName, "/images/profile/upload");
		}

		return userRepository.save(user);
	}

	// ===============================
	// 로그인
	// ===============================
	public JwtToken login(LoginRequestDto request) {

		UserEntity user = userRepository.findByMemberId(request.getMemberId())
				.orElseThrow(() -> new UnauthorizedException("ID 또는 비밀번호가 틀렸습니다."));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new UnauthorizedException("ID 또는 비밀번호가 틀렸습니다.");
		}

		String accessToken = jwtProvider.createAccessToken(
				user.getId(),
				user.getRole());

		String refreshToken = jwtProvider.createRefreshToken(user.getId());
		refreshTokenRepository.save(user.getId(), refreshToken);

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

		UserEntity user = userRepository.findById(memberId)
				.orElseThrow(() -> new UnauthorizedException("회원이 존재하지 않습니다."));

		String newAccessToken = jwtProvider.createAccessToken(
				memberId,
				user.getRole());

		return new JwtToken(newAccessToken, refreshToken);
	}

	// ===============================
	// 프로필 이미지 변경 (추가)
	// ===============================
	@Transactional
	public void updateProfileImage(Long userId, MultipartFile image) {

		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new BadRequestException("회원 없음"));

		String savedName = fileStorageService.saveProfileImage(image);

		user.updateProfileImage(
				savedName,
				"/images/profile/upload");
	}

	// ===============================
	// 프로필 이미지 수정 (추가)
	// ===============================
	@Transactional
	public void updateProfile(Long userId, String intro, String levelLanguage) {

		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new BadRequestException("회원 없음"));

		LanguageLevel level = null;
		if (levelLanguage != null) {
			level = LanguageLevel.valueOf(levelLanguage);
		}

		user.updateProfile(intro, level);
	}
}