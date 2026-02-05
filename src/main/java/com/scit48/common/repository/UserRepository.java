package com.scit48.common.repository;

import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import com.scit48.common.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 회원 조회용 Repository
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	// 닉네임으로 회원 찾기 (중복 확인 및 프로필 조회용)
	Optional<UserEntity> findByNickname(String nickname);
	//gender
	List<UserEntity> findByGenderAndNation(Gender targetGender, String targetCountry);

    Optional<UserEntity> findByMemberId(String memberId);

    boolean existsByMemberId(String memberId);

    boolean existsByNickname(String nickname);
	
	List<UserEntity> findByGenderAndNationAndIdNot(
			Gender targetGender,
			String targetCountry,
			Long Id
	);
	
	// 오늘 가입한 회원 수
	long countByCreatedAtAfter(LocalDateTime time);
	
	@Query(
			value = """
    SELECT
      DATE(created_at) AS label,
      COUNT(*) AS value
    FROM users
    WHERE created_at >= DATE_SUB(CURRENT_DATE, INTERVAL 6 DAY)
    GROUP BY DATE(created_at)
    ORDER BY DATE(created_at)
  """,
			nativeQuery = true
	)
	List<Object[]> countStatsDaily();

	
	
}
