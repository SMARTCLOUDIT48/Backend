package com.scit48.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.scit48.common.enums.Gender;
import com.scit48.common.enums.LanguageLevel;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String memberId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, length = 10)
    private String nation;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal manner = new BigDecimal("36.5");

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(length = 255)
    private String profileImageName;

    @Column(length = 255)
    private String profileImagePath;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 10)
    private String nativeLanguage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LanguageLevel levelLanguage;

    // 생성자
    public MemberEntity(
            String memberId,
            String password,
            String nickname,
            Gender gender,
            int age,
            String nation,
            String nativeLanguage,
            LanguageLevel levelLanguage) {
        this.memberId = memberId;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.nation = nation;
        this.nativeLanguage = nativeLanguage;
        this.levelLanguage = levelLanguage;
        this.role = "ROLE_MEMBER";
        this.createdAt = LocalDateTime.now();
    }

}
