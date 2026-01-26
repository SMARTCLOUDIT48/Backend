package com.scit48.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scit48.common.domain.entity.UserEntity;

import java.util.Optional;

/**
 * 회원 조회용 Repository
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByMemberId(String memberId);

    Optional<UserEntity> findByNickname(String nickname);

    boolean existsByMemberId(String memberId);

    boolean existsByNickname(String nickname);
}
