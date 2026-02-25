package com.scit48.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scit48.common.repository.UserRepository;
import com.scit48.common.repository.ProfileViewRepository;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.domain.entity.ProfileViewEntity;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileViewService {

    private final ProfileViewRepository repository;
    private final UserRepository userRepository;

    // 프로필 조회 기록
    @Transactional
    public void recordView(Long viewerId, Long targetId) {

        if (viewerId.equals(targetId))
            return;

        repository.save(
                ProfileViewEntity.builder()
                        .viewerId(viewerId)
                        .targetId(targetId)
                        .viewedAt(LocalDateTime.now())
                        .build());
    }

    // 방문자 목록
    @Transactional(readOnly = true)
    public List<UserDTO> getRecentVisitors(Long myId) {

        List<Long> ids = repository
                .findTop20ByTargetIdOrderByViewedAtDesc(myId)
                .stream()
                .map(ProfileViewEntity::getViewerId)
                .distinct()
                .toList();

        return userRepository.findAllById(ids)
                .stream()
                .map(UserDTO::fromEntity)
                .toList();
    }
}