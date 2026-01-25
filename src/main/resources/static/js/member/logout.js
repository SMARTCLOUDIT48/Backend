async function logout() {
    await authFetch(`${CONTEXT_PATH}api/logout`, {
        method: "POST"
    });

    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");

    alert("로그아웃 되었습니다.");
    location.href = `${CONTEXT_PATH}login`;
}
