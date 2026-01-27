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
  console.log("authFetch 요청:", url);

  let response = await fetch(url, {
    ...options,
    credentials: "include"
  });

  if (response.status !== 401) {
    return response;
  }

  //  reissue 요청 자체에는 재발급 로직 적용 금지
  if (url.includes("/api/reissue")) {
    return response;
  }

  console.warn("401 발생 → accessToken 재발급 시도");

  const reissueResponse = await fetch(`${CONTEXT_PATH}api/reissue`, {
    method: "POST",
    credentials: "include"
  });

  if (!reissueResponse.ok) {
    alert("로그인이 필요합니다.");
    location.href = `${CONTEXT_PATH}login`;
    return;
  }

  //  재발급 성공 → 원래 요청 딱 1번만 재시도
  return fetch(url, {
    ...options,
    credentials: "include"
  });
}
