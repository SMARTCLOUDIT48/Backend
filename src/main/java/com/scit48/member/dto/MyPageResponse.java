package com.scit48.member.dto;

import com.scit48.common.enums.Gender;

public record MyPageResponse(
        String memberId,
        String nickname,
        Gender gender,
        int age,
        String nation,
        double manner,
        String intro,
        String profileImagePath) {
}
