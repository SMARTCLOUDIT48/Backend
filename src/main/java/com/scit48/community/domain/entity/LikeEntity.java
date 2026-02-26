package com.scit48.community.domain.entity;

import com.scit48.common.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "likes")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LikeEntity {
	
	@EmbeddedId
	private LikeKey likeId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private UserEntity user;
	
	@MapsId("boardId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "board_id")
	private BoardEntity board;
	
	@CreatedDate
	@Column(name = "created_At", nullable = false,
			insertable = false,
			updatable = false,
			columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime inputDate;
	
	
	
	
	public LikeEntity(UserEntity user, BoardEntity board) {
		this.user = user;
		this.board = board;
	}
}
