package com.scit48.common.key;

import java.time.LocalDate;

public final class RedisKeyFactory {

    private RedisKeyFactory() {
    }

    /*
     * =========================
     * CHAT
     * =========================
     */

    // chat:room:count:{roomId}
    public static String chatRoomCount(String roomId) {
        return "chat:room:count:" + roomId;
    }

    // user:daily:interaction:{userId}:{date}
    public static String dailyInteraction(long userId, LocalDate date) {
        return "user:daily:interaction:" + userId + ":" + date;
    }

    /*
     * =========================
     * AUTH
     * =========================
     */

    // auth:refresh:user_id:{userId}
    public static String refreshToken(long userId) {
        return "auth:refresh:user_id_" + userId;
    }
}
