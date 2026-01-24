package com.scit48.auth.dto;

import com.scit48.common.enums.Gender;
import com.scit48.common.enums.LanguageLevel;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDto {
    private String memberId;
    private String password;
    private String nickname;
    private Gender gender;
    private int age;
    private String nation;
    private String nativeLanguage;
    private LanguageLevel levelLanguage;
}
