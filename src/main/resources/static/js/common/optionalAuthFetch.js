/**
 * optionalAuthFetch
 * ----------------------------------------
 * 인증이 "선택"인 API 요청용 fetch 래퍼
 *
 * ✔ 로그인/비로그인 모두 접근 가능
 * ✔ 쿠키 기반 자동 인증
 * ✔ 401 발생 시 한 번만 재발급 시도
 * ✔ 실패해도 로그인 강제 이동 ❌
 */
export async function optionalAuthFetch(url, options = {}) {

    let response = await fetch(url, {
        ...options,
        credentials: "include"
    });

    // 정상 응답이면 그대로 반환
    if (response.status !== 401) {
        return response;
    }

    // 401 → accessToken 만료 가능성
    //  조용히 재발급 시도
    const reissueResponse = await fetch(`${CONTEXT_PATH}api/reissue`, {
        method: "POST",
        credentials: "include"
    });

    // 재발급 성공 → 요청 재시도
    if (reissueResponse.ok) {
        return fetch(url, {
            ...options,
            credentials: "include"
        });
    }

    // 재발급 실패 → 그냥 401 반환 (비로그인 취급)
    return response;
}

