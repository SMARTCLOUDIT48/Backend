package com.scit48.auth.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.domain.entity.UserInterestEntity;
import com.scit48.common.dto.UserInterestDTO;
import com.scit48.common.repository.UserInterestRepository;
import com.scit48.common.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberInterestService {

        private final UserInterestRepository repository;
        private final UserRepository userRepository;

        /*
         * =========================
         * 관심사 저장 / 수정
         * =========================
         */
        @Transactional
        public void saveUserInterests(Long userId, List<UserInterestDTO> interests) {

                UserEntity user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

                // 기존 관심사 삭제
                repository.deleteByUser_Id(userId);

                // 반드시 flush
                repository.flush();

                if (interests == null || interests.isEmpty()) {
                        return;
                }

                List<UserInterestEntity> entities = interests.stream()
                                .map(dto -> UserInterestEntity.builder()
                                                .user(user)
                                                .interest(dto.getInterest())
                                                .interestDetail(dto.getInterestDetail())
                                                .build())
                                .toList();

                repository.saveAll(entities);
        }

        /*
         * =========================
         * 관심사 조회
         * =========================
         */
        @Transactional(readOnly = true)
        public List<UserInterestDTO> getUserInterests(Long userId) {

                return repository.findByUser_Id(userId)
                                .stream()
                                .map(UserInterestDTO::fromEntity)
                                .toList();
        }
}
