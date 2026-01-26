async function logout() {
  try {
    const res = await fetch(`${CONTEXT_PATH}api/logout`, {
      method: "POST",
      credentials: "include" //  쿠키 보내기
    });

    if (!res.ok) {
      alert("로그아웃 처리 중 문제가 발생했습니다.");
      return;
    }

    alert("로그아웃 되었습니다.");
    location.href = `${CONTEXT_PATH}login`;

  } catch (e) {
    alert("네트워크 오류로 로그아웃에 실패했습니다.");
  }
}
