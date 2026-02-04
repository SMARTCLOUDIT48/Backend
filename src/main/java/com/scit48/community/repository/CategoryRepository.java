package com.scit48.community.repository;

import com.scit48.community.domain.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
	// 카테고리 이름으로 엔티티 조회
	Optional<CategoryEntity> findByName(String name);
	
	List<CategoryEntity> findByNameNot(String 일상);
}
