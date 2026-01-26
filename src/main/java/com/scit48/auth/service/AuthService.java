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
import lombok.extern.slf4j.Slf4j; // 로그를 위해 추가하면 좋지만, System.out으로 유지합니다.
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional // DB 저장을 위해 트랜잭션 추가
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
		
		// 👇 [범인 검거용 로그] 이 부분이 실행되면서 콘솔에 값이 찍힙니다!
		System.out.println("=====================================");
		System.out.println("🚨 [로그 확인] 들어온 NativeLanguage 값: " + request.getNativeLanguage());
		if (request.getNativeLanguage() != null) {
			System.out.println("🚨 [로그 확인] 데이터 길이: " + request.getNativeLanguage().length());
		} else {
			System.out.println("🚨 [로그 확인] 데이터 길이: NULL");
		}
		System.out.println("=====================================");
		
		// 1. 중복 체크
		if (memberRepository.existsByMemberId(request.getMemberId())) {
			throw new BadRequestException("이미 존재하는 ID입니다.");
		}
		
		if (memberRepository.existsByNickname(request.getNickname())) {
			throw new BadRequestException("이미 사용 중인 닉네임입니다.");
		}
		
		// 2. 엔티티 생성
		UserEntity member = UserEntity.builder()
				.memberId(request.getMemberId())
				.password(passwordEncoder.encode(request.getPassword()))
				.nickname(request.getNickname())
				.gender(request.getGender())
				.age(request.getAge())
				.nation(request.getNation())
				.nativeLanguage(request.getNativeLanguage()) // 👈 여기에 토큰이 들어오는지 의심됨
				.levelLanguage(request.getLevelLanguage())
				.role("ROLE_MEMBER")
				.build();
		
		// 3. 프로필 이미지 저장 (이미지가 있을 경우에만)
		if (image != null && !image.isEmpty()) {
			String savedName = fileStorageService.save(image);
			member.updateProfileImage(savedName, "/images/" + savedName);
		}
		
		// 4. DB 저장 후 반환
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