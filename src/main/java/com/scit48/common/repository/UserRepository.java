package com.scit48.common.repository;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	// 닉네임으로 회원 찾기 (중복 확인 및 프로필 조회용)
	Optional<UserEntity> findByNickname(String nickname);
	//gender
	List<UserEntity> findBygenderAndnation(Gender targetGender, String targetCountry);
}
