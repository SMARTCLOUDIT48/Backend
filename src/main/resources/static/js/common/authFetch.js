/**
 * authFetch
 * ----------------------------------------
 * 인증이 "필수"인 API 요청용 fetch 래퍼
 *
 * ✔ HttpOnly Cookie 기반 인증
 * ✔ credentials: "include" 사용
 * ✔ 401 → 토큰 재발급 시도
 * ✔ 재발급 실패 시 로그인 페이지 이동
 */
export async function authFetch(url, options = {}) {

    // 1차 요청
    let response = await fetch(url, {
        ...options,
        credentials: "include"
    });

    // 정상 응답이면 그대로 반환
    if (response.status !== 401) {
        return response;
    }

    // 401 → accessToken 만료 가능성
    //  refreshToken으로 재발급 시도
    const reissueResponse = await fetch(`${CONTEXT_PATH}api/reissue`, {
        method: "POST",
        credentials: "include"
    });

    // 재발급 성공 → 원래 요청 다시 시도
    if (reissueResponse.ok) {
        return fetch(url, {
            ...options,
            credentials: "include"
        });
    }

    // 재발급도 실패 → 진짜 로그아웃 상태
    alert("로그인이 필요합니다.");
    location.href = `${CONTEXT_PATH}login`;
    return;
}
