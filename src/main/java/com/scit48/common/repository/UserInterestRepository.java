package com.scit48.common.repository;

import com.scit48.common.domain.entity.UserInterestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInterestRepository  extends JpaRepository <UserInterestEntity, Long> {
}
