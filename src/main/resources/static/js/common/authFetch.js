/**
 * authFetch
 * ----------------------------------------
 * 인증이 필요한 API 요청용 fetch 래퍼
 *
 * ✔ Authorization 헤더 자동 추가
 * ✔ accessToken은 localStorage에서 가져옴
 * ✔ 토큰 없으면 로그인 페이지로 이동
 * ✔ 401(토큰 만료/인증 실패) 시 토큰 제거 후 로그인 이동
 *
 * 사용 대상:
 * - 로그인 이후 접근해야 하는 모든 API
 */

export async function authFetch(url, options = {}) {
    const accessToken = localStorage.getItem("accessToken");

    // 로그인 안 된 상태
    if (!accessToken) {
        alert("로그인이 필요합니다.");
        location.href = `${CONTEXT_PATH}login`;
        return;
    }

    // 기존 헤더 유지 + Authorization 추가
    const headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${accessToken}`
    };

    const response = await fetch(url, {
        ...options,
        headers
    });

    // 토큰 만료 / 인증 실패
    if (response.status === 401) {
        alert("로그인이 만료되었습니다. 다시 로그인해주세요.");

        // 토큰 삭제
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");

        location.href = `${CONTEXT_PATH}login`;
        return;
    }

    return response;
}
