document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const memberId = form.memberId.value.trim();
        const password = form.password.value.trim();

        if (!memberId || !password) {
            alert("ID와 비밀번호를 입력해주세요.");
            return;
        }

        const res = await fetch(`${CONTEXT_PATH}auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ memberId, password })
        });

        if (!res.ok) {
            alert("로그인 실패");
            return;
        }

        const data = await res.json();
        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("refreshToken", data.refreshToken);

        location.href = `${CONTEXT_PATH}`;
    });
});
