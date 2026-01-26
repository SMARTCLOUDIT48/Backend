package com.scit48.common.repository;

import com.scit48.common.domain.entity.UserInterestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestRepository  extends JpaRepository <UserInterestEntity, Long> {
	List<UserInterestEntity> findByuser_userId(Long user);
	
	List<UserInterestEntity> findByuser_userIdIn(List<Long> userIds);
}
