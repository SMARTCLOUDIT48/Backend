package com.scit48.recommend.service;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.domain.entity.UserInterestEntity;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.enums.Gender;
import com.scit48.common.enums.InterestDetail;
import com.scit48.common.enums.InterestType;
import com.scit48.common.enums.LanguageLevel;
import com.scit48.common.repository.UserInterestRepository;
import com.scit48.common.repository.UserRepository;
import com.scit48.recommend.domain.dto.RecommendDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RecommendService {
	
	//의존성 주입 DI
	private final UserRepository ur;
	private final UserInterestRepository uir;
	
	public List<RecommendDTO> firstRecommend(Long user) {
		
		//반환 값
		List<RecommendDTO> userPatnerDTOList = new ArrayList<>();
		
		//로그인 회원의 정보 찾기
		UserEntity loginUserEntity = ur.findById(user).orElseThrow(
				() -> new EntityNotFoundException("회원을 찾을 수 없습니다.")
		);
		//로그인 되어 있는 유저의 관심사 리스트 받아오기
		List<UserInterestEntity> loginUserInterestList = uir.findByUser_Id(user);
		
		Gender targetGender =getOppositeGender(loginUserEntity.getGender());
		String targetCountry = getOppositeCountry(loginUserEntity.getNation());
		
		
		//List <UserEntity> allUserEntitylist = ur.findByGenderAndNation(targetGender,targetCountry);
		
		//성별, 한일 반전된
		//회원 목록, 회원 관심사 전체 목록 로그인한 회원 제외
		List<UserEntity> allUserEntitylist =
				ur.findByGenderAndNationAndUserIdNot(
						targetGender,
						targetCountry,
						user
				);
		
		//allUsersEntitylist에 해당하는 id들의 리스트를 만들겠다.
		List<Long> userIds = allUserEntitylist.stream().map(UserEntity::getId).toList();
		
		//userIds로 구현된 리스트를 활용해서 id에 해당하는 리스트
		List<UserInterestEntity> userInterestEntityList= uir.findByUser_IdIn(userIds);
		
		//userInterestEntityList를 각 id에 맞게 관심사 리스트를 분리
		Map<Long, List<UserInterestEntity>> interestMap =
				userInterestEntityList.stream()
						.collect(Collectors.groupingBy(
								ui ->ui.getUser().getId()
						));
		
		//반복문 + 점수를 dto에 넣기
		for(UserEntity partner : allUserEntitylist){
			List<UserInterestEntity> partnerInterests =
					interestMap.getOrDefault(
							partner.getId(),List.of()
					);
			
			//언어 점수
			int langScore = calLangScore(
					loginUserEntity.getLevelLanguage(),partner.getLevelLanguage());
			
			// 3. 관심사 점수
			int interestScore = calInterestScore(
					loginUserInterestList,
					partnerInterests
			);
			
			// 4. 매너 점수
			int mannerScore = calMannerScore(
					partner.getManner()
			);
			// 5. 총 점수
			int totalScore = langScore + interestScore + mannerScore;
			
			RecommendDTO dto = new RecommendDTO().builder()
					.id(partner.getId())
					.nickname(partner.getNickname())
					.age(partner.getAge())
					.gender(partner.getGender())
					.nation(partner.getNation())
					.manner(partner.getManner())
					.profileImageName(partner.getProfileImageName())
					.profileImagePath(partner.getProfileImagePath())
					.nativeLanguage(partner.getNativeLanguage())
					.levelLanguage(partner.getLevelLanguage())
					.matchPoint(totalScore)
					.build();
			
			userPatnerDTOList.add(dto);
		}
		
		//리턴
		return userPatnerDTOList.stream()
				.sorted(Comparator.comparingInt(
						RecommendDTO::getMatchPoint
				).reversed())
				.limit(10)
				.toList();
		
		//로그인 한 계정을 제외하는 기능
		
	}
	
	
	
	//처음 찾을 때 성별 반전 찾기
	private Gender getOppositeGender(Gender gender){
		return gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;
	}
	// 처음 찾을 때 한일 반전 찾기
	private String getOppositeCountry(String country){
		return country.equals("KOR") ? "JPN" : "KOR";
	}
	
	/**
	 * 언어 레벨에 따른 가중치 계산 함수
	 * @param i 로그인 유저
	 * @param partner 매칭 유저
	 * @return 언어 점수 값 0~30
	 */
	private int calLangScore(LanguageLevel i, LanguageLevel partner){
		
		int diff = Math.abs(i.score() - partner.score()); //0~3 값
		return Math.max(0, 30-diff*10); //0 ~ 30
	}
	
	/**
	 * 관심사 일치에 따른 가중치 계산 함수
	 * @param i 로그인 유저
	 * @param partner 매칭 유저
	 * @return 관심사 점수 값 0 ~ 40
	 */
	private int calInterestScore(
			List<UserInterestEntity> i, List<UserInterestEntity> partner
	){
		int matchScore =0;
		
		//1. 상대방 관심사를 Map<대분류, Set<소분류>> 로 변환
		Map<InterestType, Set<InterestDetail>> partnerMap = partner.stream()
				.collect(Collectors.groupingBy(UserInterestEntity::getInterest,
						Collectors.mapping(UserInterestEntity::getInterestDetail,
								Collectors.toSet())
						)
		);
		
		//점수 계산 로직
		for (UserInterestEntity my : i) {
			InterestType myInterest = my.getInterest();
			InterestDetail myDetail = my.getInterestDetail();
			
			//대분류 일치 보장
			if (!partnerMap.containsKey(myInterest)) {
				continue;
			}
			
			Set<InterestDetail> targetDetails = partnerMap.get(myInterest);
			
			// 2. 대분류 + 소분류 동일
			if (targetDetails.contains(myDetail)) {
				matchScore += 20;
			}
			// 3. 대분류만 동일
			else {
				matchScore += 10;
			}
		}
		
		return Math.min(matchScore, 40);
	}
	
	/**
	 * 매너온도에 따른 가중치 계산 함수
	 * @param manner 파트너 매너점수
	 * @return 매너점수 -15 ~ 30  // 36.5도 일때 기본 점수 10점 0도 일때 -15 / 99.9도일때 30점
	 */
	private int calMannerScore(double manner){
		double score;
		
		// 기본값에서 +때랑 -때랑 다른 선형 그래프 적용
		if (manner >= 36.5) {
			score = 10 + (manner - 36.5) * 0.315;
		} else {
			score = 10 - (36.5 - manner) * 0.685;
		}
		
		// 상한 / 하한 제한
		score = Math.max(-15, Math.min(30, score));
		
		return (int) Math.round(score);
	}
	
}
