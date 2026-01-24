/**
 * optionalAuthFetch
 * ----------------------------------------
 *  인증이 "선택"인 API 요청용 fetch 래퍼
 *
 * ✔ 토큰이 있으면 Authorization 헤더 자동 추가
 * ✔ 토큰이 없으면 그냥 일반 fetch처럼 동작
 * ✔ 로그인 강제 이동 ❌
 *
 * 사용 대상:
 * - 메인 페이지
 * - 로그인 유무에 따라 UX만 달라지는 API
 */

export async function optionalAuthFetch(url, options = {}) {
    const accessToken = localStorage.getItem("accessToken");

    // 토큰이 없는 경우 → 그냥 fetch
    if (!accessToken) {
        return fetch(url, options);
    }

    // 토큰이 있는 경우 → Authorization 헤더 추가
    return fetch(url, {
        ...options,
        headers: {
            ...(options.headers || {}),
            Authorization: `Bearer ${accessToken}`
        }
    });
}
