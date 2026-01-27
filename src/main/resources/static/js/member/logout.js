async function logout() {
  try {
    const res = await fetch("/api/logout", {
      method: "POST",
      credentials: "include"
    });

    if (!res.ok) {
      alert("로그아웃 실패");
      return;
    }

    location.href = "/login";
  } catch (e) {
    console.error(e);
    alert("네트워크 오류로 로그아웃에 실패했습니다.");
  }
}
