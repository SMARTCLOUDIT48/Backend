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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.scit48.recommend.criteria.Criteria;

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
		//회원가입 되어 있는 유저의 관심사 리스트 받아오기
		List<UserInterestEntity> loginUserInterestList = uir.findByUser_Id(user);
		
//		Gender targetGender = getOppositeGender(loginUserEntity.getGender());
//		String targetCountry = getOppositeCountry(loginUserEntity.getNation());
		//List <UserEntity> allUserEntitylist = ur.findByGenderAndNation(targetGender,targetCountry);
		
		//성별, 한일 반전된
		//회원 목록, 회원 관심사 전체 목록 로그인한 회원 제외
		List <UserEntity> allUserEntitylist = ur.findByIdNot(user);
		
//		List<UserEntity> allUserEntitylist =
//				ur.findByGenderAndNationAndIdNot(
//						targetGender,
//						targetCountry,
//						user
//				);
		
		//allUsersEntitylist에 해당하는 id들의 리스트를 만들겠다.
		//List<Long> userIds = allUserEntitylist.stream().map(UserEntity::getId).toList();
		
		//userIds로 구현된 리스트를 활용해서 id에 해당하는 리스트
		//List<UserInterestEntity> userInterestEntityList= uir.findByUser_IdIn(userIds);
		
		return scoreAndConvert(loginUserEntity, loginUserInterestList, allUserEntitylist, 10);
	}
	
	public List<RecommendDTO> filteringSearch(Long userId, String criteriaKey) {
		Criteria c = Criteria.parse(criteriaKey);
		log.debug("parse된 필터링 키 : {}", c);
		
		//로그인 회원 정보/ 관심사 찾기(점수 계산용)
		UserEntity loginUserEntity = ur.findById(userId).orElseThrow(
				()-> new EntityNotFoundException("회원을 찾을 수 없습니다")
		);
		List<UserInterestEntity> loginUserInterestList = uir.findByUser_Id(userId);
		
		// 1️⃣ 로그인 유저 제외한 전체 유저 조회
		List<UserEntity> candidates = ur.findByIdNot(userId);
		
		// 2️⃣ Criteria 기준 필터링
		List<UserEntity> filtered = candidates.stream()
				.filter(user -> matchByCriteria(user, c))
				.toList();
		
		log.debug("필터링 통과 인원 수 : {}", filtered.size());
		//DTO 변환 하기전에 List filtered를 점수 순으로 배열
		if (filtered.isEmpty()) {
			return List.of();
		}
		// ✅ filtered 후보들의 관심사 bulk 조회
		List<Long> filteredIds = filtered.stream().map(UserEntity::getId).toList();
		List<UserInterestEntity> filteredInterestEntities = uir.findByUser_IdIn(filteredIds);
		
		
//		// 3️⃣ DTO 변환
//		List<RecommendDTO> filteredResult = filtered.stream()
//				.map(user -> RecommendDTO.fromEntity(user))
//				.toList();
		
		return scoreAndConvert(loginUserEntity, loginUserInterestList, filtered, 10);
	}
	
	
	//처음 찾을 때 성별 반전 찾기
	private Gender getOppositeGender(Gender gender){
		return gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;
	}
	// 처음 찾을 때 한일 반전 찾기
	private String getOppositeCountry(String country){
		return country.equals("KOREA") ? "JAPAN" : "KOREA";
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
	
	public Long searchid(UserDetails user) {
		UserEntity entity = ur.findByMemberId(user.getUsername()).orElseThrow(
				()-> new EntityNotFoundException("회원을 찾을 수 없습니다.")
		);
		
		return entity.getId();
	}
	
	private boolean matchByCriteria(UserEntity u, Criteria c) {
		// 성별
		if (!"ANY".equals(c.getGender())) {
			if (u.getGender() == null) return false;
			if (!u.getGender().name().equals(c.getGender())) return false;
		}
		
		// 나이
		int age = u.getAge();
		if (age < c.getAgeMin() || age > c.getAgeMax()) return false;
		
		// 국적
		if (!"ANY".equals(c.getNation())) {
			if (u.getNation() == null) return false;
			if (!u.getNation().equalsIgnoreCase(c.getNation())) return false;
		}
		
		// 학습 언어
		if (!"ANY".equals(c.getStudyLang())) {
			if (u.getStudyLanguage() == null) return false;
			if (!u.getStudyLanguage().equals(c.getStudyLang())) return false;
		}
		
		// 언어 레벨
		if (!c.isLevelsAny()) {
			if (u.getLevelLanguage() == null) return false;
			
			
			LanguageLevel level = u.getLevelLanguage(); // 구조에 맞게 수정
			int level_int = levelToInt(level); //숫자로 변경
			if (!c.getLevels().contains(level_int)) return false;
		}
		
		// 관심사 맘에 걸리는 데
		if (!c.isInterestsAny()) { //Interest가 비어있지 않으면
			//user Id를 기반으로 관심사 항목을 리스트로 받아옴
			List <UserInterestEntity> InterestEntityList= uir.findByUser_Id(u.getId());
			
			//받아온 관심사 List 목록을 Set으로 변경
			Set<InterestType> userInterests = InterestEntityList.stream()
					.map(UserInterestEntity::getInterest)
					.collect(Collectors.toSet());
			
			// c 필터링 항목 c에 맞는게 한개라도 있으면 true
			boolean hasCommon = userInterests.stream()
					.anyMatch(c.getInterests()::contains);
			// 아니면 false
			if (!hasCommon) return false;
		}
		// 전부 통과하면 true 반환
		return true;
	}
	
	private int levelToInt(LanguageLevel lv) {
		if (lv == null) return 1;
		return switch (lv) {
			case BEGINNER -> 1;
			case INTERMEDIATE -> 2;
			case ADVANCED -> 3;
			case NATIVE -> 4;
		};
	}
	
	/**
	 * ✅ 공통: 후보 리스트에 대해
	 * - 관심사 bulk 조회 → 점수 계산 → DTO 변환 → 점수 내림차순 정렬 → 상위 10개
	 */
	private List<RecommendDTO> scoreAndConvert(
			UserEntity loginUser,
			List<UserInterestEntity> loginInterests,
			List<UserEntity> partners,
			int limit
	) {
		if (partners == null || partners.isEmpty()) {
			return List.of();
		}
		
		Map<Long, List<UserInterestEntity>> interestMap = buildInterestMap(partners);
		
		return partners.stream()
				.map(partner -> {
					List<UserInterestEntity> partnerInterests =
							interestMap.getOrDefault(partner.getId(), List.of());
					
					int langScore = calLangScore(
							loginUser.getLevelLanguage(),
							partner.getLevelLanguage()
					);
					
					int interestScore = calInterestScore(
							loginInterests,
							partnerInterests
					);
					
					int mannerScore = calMannerScore(partner.getManner());
					
					int totalScore = langScore + interestScore + mannerScore;
					
					return RecommendDTO.builder()
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
							.studyLanguage(partner.getStudyLanguage())
							.matchPoint(totalScore)
							.build();
				})
				.sorted(Comparator.comparingInt(RecommendDTO::getMatchPoint).reversed())
				.limit(limit)
				.toList();
	}
	
	/**
	 * ✅ 공통: partners에 속한 유저들의 관심사를 한 번에 조회해서
	 * userId -> 관심사 리스트로 그룹핑
	 */
	private Map<Long, List<UserInterestEntity>> buildInterestMap(List<UserEntity> partners) {
		List<Long> ids = partners.stream().map(UserEntity::getId).toList();
		
		List<UserInterestEntity> interestEntities = uir.findByUser_IdIn(ids);
		
		return interestEntities.stream()
				.collect(Collectors.groupingBy(ui -> ui.getUser().getId()));
	}
	
}
