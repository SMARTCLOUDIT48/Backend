package com.scit48.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scit48.common.repository.UserRepository;
import com.scit48.common.repository.ProfileViewRepository;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.domain.entity.ProfileViewEntity;
import com.scit48.common.domain.entity.UserEntity;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileViewService {

    private final ProfileViewRepository repository;
    private final UserRepository userRepository;

    // 하루 1회 방문 기록
    @Transactional
    public void recordView(Long viewerId, Long targetId) {

        if (viewerId.equals(targetId))
            return;

        LocalDate today = LocalDate.now();

        var optional = repository.findByViewerIdAndTargetIdAndViewDate(
                viewerId, targetId, today);

        if (optional.isPresent()) {
            return; // 오늘 이미 방문 → 무시
        }

        repository.save(
                ProfileViewEntity.builder()
                        .viewerId(viewerId)
                        .targetId(targetId)
                        .viewDate(today)
                        .viewedAt(LocalDateTime.now())
                        .build());
    }

    // 최근 방문자 20명
    @Transactional(readOnly = true)
    public List<UserDTO> getRecentVisitors(Long myId) {

        List<Long> ids = repository
                .findTop20ByTargetIdOrderByViewedAtDesc(myId)
                .stream()
                .map(ProfileViewEntity::getViewerId)
                .distinct()
                .toList();

        List<UserEntity> users = userRepository.findAllById(ids);

        Map<Long, UserEntity> map = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));

        return ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .map(UserDTO::fromEntity)
                .toList();
    }
}