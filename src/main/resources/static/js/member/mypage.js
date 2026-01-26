import { authFetch } from "/js/common/authFetch.js";

document.addEventListener("DOMContentLoaded", async () => {
    // 프론트에서도 1차 방어 (UX용)
    const token = localStorage.getItem("accessToken");
    if (!token) {
        location.href = `${CONTEXT_PATH}login`;
        return;
    }

    try {
        // 인증 fetch 반드시 사용
        const response = await authFetch("/api/members/me");
        if (!response) return;

        const result = await response.json();

        if (!result.success) {
            console.error("마이페이지 데이터 조회 실패");
            return;
        }

        const user = result.data;

        // ===== DOM 바인딩 =====
        const nicknameEl = document.getElementById("nickname");
        const languageEl = document.getElementById("language");
        const mannerEl = document.getElementById("manner");
        const profileImageEl = document.getElementById("profileImage");

        if (nicknameEl) {
            nicknameEl.textContent = user.nickname ?? "";
        }

        if (languageEl) {
            const native = user.nativeLanguage ?? "";
            const level = user.levelLanguage ?? "";
            languageEl.textContent = `${native} → ${level}`;
        }

        if (mannerEl) {
            mannerEl.textContent = `${user.manner}°C`;
        }

        if (profileImageEl && user.profileImagePath) {
            profileImageEl.src = user.profileImagePath;
        }

    } catch (error) {
        console.error("마이페이지 로딩 중 오류", error);
    }
});
