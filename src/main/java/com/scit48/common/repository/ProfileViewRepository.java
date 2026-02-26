package com.scit48.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.scit48.common.domain.entity.ProfileViewEntity;

public interface ProfileViewRepository
        extends JpaRepository<ProfileViewEntity, Long> {

    Optional<ProfileViewEntity> findByViewerIdAndTargetIdAndViewDate(
            Long viewerId, Long targetId, LocalDate viewDate);

    List<ProfileViewEntity> findTop20ByTargetIdOrderByViewedAtDesc(Long targetId);
}