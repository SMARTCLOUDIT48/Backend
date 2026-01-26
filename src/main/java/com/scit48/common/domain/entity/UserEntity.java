package com.scit48.common.domain.entity;

import com.scit48.common.enums.Gender;
import com.scit48.common.enums.LanguageLevel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "member_id"),
        @UniqueConstraint(columnNames = "nickname")
})
public class UserEntity {

    /*
     * =========================
     * PK
     * =========================
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /*
     * =========================
     * 인증 정보
     * =========================
     */
    @Column(name = "member_id", nullable = false, length = 50)
    private String memberId;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String role = "ROLE_MEMBER";

    /*
     * =========================
     * 프로필 정보
     * =========================
     */
    @Column(nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 10)
    private String nation;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(name = "native_language", nullable = false, length = 10)
    private String nativeLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "level_language", nullable = false, length = 20)
    private LanguageLevel levelLanguage;

    /*
     * =========================
     * 매너 / 이미지
     * =========================
     */
    @Builder.Default
    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal manner = BigDecimal.valueOf(36.5);

    @Column(name = "profile_image_name")
    private String profileImageName;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    /*
     * =========================
     * 생성일
     * =========================
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /*
     * =========================
     * 비즈니스 메서드
     * =========================
     */
    public void updateProfileImage(String name, String path) {
        this.profileImageName = name;
        this.profileImagePath = path;
    }
}
