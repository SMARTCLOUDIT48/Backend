package com.scit48.community.service;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.enums.InterestType;
import com.scit48.common.repository.UserInterestRepository;
import com.scit48.common.repository.UserRepository;
import com.scit48.community.repository.BoardRepository;
import com.scit48.community.repository.CategoryRepository;
import com.scit48.recommend.domain.dto.RecommendDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ProfileService {
	
	private final BoardRepository br;
	private final CategoryRepository ctr;
	private final UserRepository ur;
	private final UserInterestRepository userInterestRepository;
	
	/**
	 * 특정 사용자의 아이디(memberId)로 프로필 정보를 조회하여 DTO로 반환합니다.
	 */
	@Transactional
	public UserDTO findByMemberId(String memberId) {
		
		// 1. DB에서 memberId로 유저 엔티티 조회
		UserEntity user = ur.findByMemberId(memberId)
				.orElseThrow(() -> new EntityNotFoundException("해당 사용자를 찾을 수 없습니다."));
		
		// 2. 엔티티를 DTO로 변환하여 반환 (빌더 패턴 사용)
		return UserDTO.builder()
				.id(user.getId())
				.memberId(user.getMemberId())
				.nickname(user.getNickname())
				.gender(user.getGender())
				.intro(user.getIntro())
				.age(user.getAge())
				.manner(user.getManner())
				.likeCount(user.getLikeCount())
				.nativeLanguage(user.getNativeLanguage())
				.studyLanguage(user.getStudyLanguage())
				.levelLanguage(user.getLevelLanguage())
				.profileImageName(user.getProfileImageName())
				.profileImagePath(user.getProfileImagePath())
				.nation(user.getNation())
				// 필요시 userPage.html에서 보여줄 다른 정보 추가
				.build();
	}
	
	
	@Transactional
	public List<InterestType> getUserInterests(String memberId) {
		
		UserEntity me = ur.findByMemberId(memberId)
				.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
		
		return userInterestRepository.findInterestsByUserId(me.getId());
		
	}
	
	
	// [수정된 메서드] 추천 유저 목록 조회 (이전 답변에서 교정한 코드 그대로 사용)
	@Transactional
	public List<RecommendDTO> getRecommendedUsers(String memberId) {
		
		UserEntity me = ur.findByMemberId(memberId)
				.orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
		List<InterestType> myInterests = userInterestRepository.findInterestsByUserId(me.getId());
		
		List<UserEntity> targetUsers = ur.findAllExceptMe(memberId);
		
		return targetUsers.stream().map(targetUser -> {
					List<InterestType> targetInterests = userInterestRepository.findInterestsByUserId(targetUser.getId());
					
					int matchPoint = calculateMatchPoint(myInterests, targetInterests);
					
					return RecommendDTO.builder()
							.id(targetUser.getId())
							.nickname(targetUser.getNickname())
							// ... 나머지 매핑 동일 ...
							.matchPoint(matchPoint)
							.interests(targetInterests)
							.getTodayInteractionCount(0L)
							.build();
				})
				.sorted((a, b) -> Integer.compare(b.getMatchPoint(), a.getMatchPoint()))
				.collect(Collectors.toList());
	}
	// 공통 관심사 개수에 따라 매칭 점수를 계산하는 헬퍼 메서드 (예시)
	private int calculateMatchPoint(List<InterestType> me, List<InterestType> target) {
		if (me == null || target == null) return 0;
		
		long sharedCount = me.stream()
				.filter(target::contains)
				.count();
		
		// 겹치는 관심사 1개당 20점 (최대 100점) 등으로 계산
		return (int) Math.min(sharedCount * 20, 100);
	}
	
}
