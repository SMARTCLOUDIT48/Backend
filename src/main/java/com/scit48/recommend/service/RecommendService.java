package com.scit48.recommend.service;

import com.scit48.common.dto.UserDTO;
import com.scit48.common.repository.UserInterestRepository;
import com.scit48.common.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RecommendService {
	
	//의존성 주입 DI
	private final UserRepository ur;
	private final UserInterestRepository uir;
	
	public List<UserDTO> firstRecommend(String user) {
		
		int matchPoint = 0;
		List<UserDTO> userDTOList = new ArrayList<>();
		
		
		
		return userDTOList;
	}
}
