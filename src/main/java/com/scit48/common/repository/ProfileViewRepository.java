package com.scit48.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.scit48.common.domain.entity.ProfileViewEntity;

public interface ProfileViewRepository
        extends JpaRepository<ProfileViewEntity, Long> {

    List<ProfileViewEntity> findTop20ByTargetIdOrderByViewedAtDesc(Long targetId);
}