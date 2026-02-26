package com.scit48.community.service;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.enums.InterestType;
import com.scit48.common.repository.UserInterestRepository;
import com.scit48.common.repository.UserRepository;
import com.scit48.community.domain.dto.BoardDTO;
import com.scit48.community.domain.entity.BoardEntity;
import com.scit48.community.repository.BoardRepository;
import com.scit48.community.repository.CategoryRepository;
import com.scit48.recommend.domain.dto.RecommendDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	
	@Transactional
	public Page<BoardDTO> getUserBoards(String memberId, Pageable pageable, String searchType, String searchKeyword) {
		
		Page<BoardEntity> boardEntities;
		
		// 1. 검색어가 있는 경우 분기 처리
		if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
			if ("title".equals(searchType)) {
				boardEntities = br.findByMemberIdAndTitle(memberId, searchKeyword, pageable);
			} else if ("content".equals(searchType)) {
				boardEntities = br.findByMemberIdAndContent(memberId, searchKeyword, pageable);
			} else if ("titleContent".equals(searchType)) {
				boardEntities = br.findByMemberIdAndTitleOrContent(memberId, searchKeyword, pageable);
			} else {
				boardEntities = br.findAllByMemberId(memberId, pageable);
			}
		} else {
			// 2. 검색어가 없는 경우 전체 목록 조회
			boardEntities = br.findAllByMemberId(memberId, pageable);
		}
		
		// 3. Page<Entity> 를 Page<DTO> 로 변환하여 반환
		return boardEntities.map(entity -> BoardDTO.builder()
				.id(entity.getUser().getId())
				.title(entity.getTitle())
				.content(entity.getContent())
				.viewCount(entity.getViewCount())
				.createdDate(entity.getCreatedAt())
				.boardId(entity.getBoardId())
				// 필요하다면 좋아요 수 등 다른 필드도 매핑
				.build()
		);
	}
	
}
