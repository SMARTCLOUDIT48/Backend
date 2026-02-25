package com.scit48.common.service;

import java.util.Optional;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.scit48.common.domain.entity.UserReactionEntity;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.enums.ReactionType;
import com.scit48.common.repository.UserReactionRepository;
import com.scit48.common.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserReactionService {

    private static final double MANNER_DELTA = 0.2;

    private final UserRepository userRepository;
    private final UserReactionRepository reactionRepository;

    /**
     * 좋아요 / 싫어요 처리
     */
    @Transactional
    public void react(Long fromUserId, Long toUserId, ReactionType newReaction) {

        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException("자기 자신에게는 할 수 없습니다.");
        }

        Optional<UserReactionEntity> optional = reactionRepository.findByFromUserIdAndToUserId(fromUserId, toUserId);

        // 처음 반응
        if (optional.isEmpty()) {
            reactionRepository.save(
                    UserReactionEntity.builder()
                            .fromUserId(fromUserId)
                            .toUserId(toUserId)
                            .reaction(newReaction)
                            .build());
            applyReaction(toUserId, newReaction, true);
            return;
        }

        UserReactionEntity reaction = optional.get();

        // 같은 반응 다시 누름 → 취소
        if (reaction.getReaction() == newReaction) {
            reactionRepository.delete(reaction);
            applyReaction(toUserId, newReaction, false);
            return;
        }

        // LIKE ↔ DISLIKE 변경
        ReactionType old = reaction.getReaction();
        reaction.changeReaction(newReaction);

        applyReaction(toUserId, old, false);
        applyReaction(toUserId, newReaction, true);
    }

    /**
     * 실제 수치 반영
     */
    private void applyReaction(Long userId, ReactionType reaction, boolean apply) {

        double delta = apply ? MANNER_DELTA : -MANNER_DELTA;

        switch (reaction) {
            case LIKE -> {
                userRepository.updateLikeCount(userId, apply ? 1 : -1);
                userRepository.updateManner(userId, delta);
            }
            case DISLIKE -> {
                userRepository.updateManner(userId, -delta);
            }
        }
    }

    /**
     * 나를 좋아요 누른 사람 목록
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersWhoLikedMe(Long myUserId) {

        // 좋아요 누른 사람 ID 목록
        List<Long> fromUserIds = reactionRepository
                .findByToUserIdAndReaction(myUserId, ReactionType.LIKE)
                .stream()
                .map(UserReactionEntity::getFromUserId)
                .toList();

        if (fromUserIds.isEmpty()) {
            return List.of();
        }

        // UserEntity 조회 후 DTO 변환
        List<UserEntity> users = userRepository.findAllById(fromUserIds);

        return users.stream()
                .map(UserDTO::fromEntity)
                .toList();
    }
}