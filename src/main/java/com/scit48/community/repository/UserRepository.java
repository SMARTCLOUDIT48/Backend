package com.scit48.community.repository;

import com.scit48.community.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	// 닉네임으로 회원 찾기 (중복 확인 및 프로필 조회용)
	Optional<UserEntity> findByNickname(String nickname);
}
