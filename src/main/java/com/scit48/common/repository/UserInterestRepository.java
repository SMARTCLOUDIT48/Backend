package com.scit48.common.repository;

import com.scit48.common.domain.entity.UserInterestEntity;
import com.scit48.common.enums.InterestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterestEntity, Long> {

	List<UserInterestEntity> findByUser_Id(Long user);

	List<UserInterestEntity> findByUser_IdIn(List<Long> userIds);

	@org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
	void deleteByUser_Id(Long user);
	
	
	@Query("SELECT ui.interest FROM UserInterestEntity ui WHERE ui.id = :userId")
	List<InterestType> findInterestsByUserId(@Param("userId") Long userId);
}
