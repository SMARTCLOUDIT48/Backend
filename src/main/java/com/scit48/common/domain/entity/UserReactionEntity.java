package com.scit48.common.domain.entity;

import jakarta.persistence.*;
import com.scit48.common.enums.ReactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_reactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "from_user_id", "to_user_id" })
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType reaction;

    public void changeReaction(ReactionType reaction) {
        this.reaction = reaction;
    }
}
