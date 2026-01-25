package com.scit48.member.dto;

import com.scit48.common.enums.Gender;
import com.scit48.common.enums.LanguageLevel;

public record MyPageResponse(
                String memberId,
                String nickname,
                Gender gender,
                Integer age,
                String nation,
                double manner,
                String intro,
                String profileImagePath,
                String nativeLanguage,
                LanguageLevel levelLanguage) {
}
