package com.scit48.common.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "profile_view", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_view", columnNames = { "viewer_id", "target_id", "view_date" })
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "viewer_id", nullable = false)
    private Long viewerId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate; // 중복 방지용 날짜

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;
}