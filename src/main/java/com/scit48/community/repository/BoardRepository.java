package com.scit48.community.repository;

import com.scit48.community.domain.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository
			extends JpaRepository<BoardEntity, Integer> {
	
}
