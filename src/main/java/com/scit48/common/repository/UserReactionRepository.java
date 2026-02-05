package com.scit48.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scit48.common.domain.entity.UserReactionEntity;
import com.scit48.common.enums.ReactionType;

public interface UserReactionRepository
                extends JpaRepository<UserReactionEntity, Long> {

        Optional<UserReactionEntity> findByFromUserIdAndToUserId(
                        Long fromUserId,
                        Long toUserId);

        List<UserReactionEntity> findByToUserIdAndReaction(
                        Long toUserId,
                        ReactionType reaction);
}
