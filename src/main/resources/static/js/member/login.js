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

        try {
            const res = await fetch("/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    memberId,
                    password
                })
            });

            const data = await res.json();

            if (!res.ok) {
                alert(data.message || "로그인에 실패했습니다.");
                return;
            }

            //  JWT 저장
            localStorage.setItem("accessToken", data.accessToken);
            localStorage.setItem("refreshToken", data.refreshToken);

            // (선택) 로그인 유지 체크 시
            if (document.getElementById("rememberMe").checked) {
                localStorage.setItem("rememberMe", "true");
            }

            //  로그인 성공 → 메인 페이지
            window.location.href = "/";

        } catch (err) {
            console.error(err);
            alert("서버 오류가 발생했습니다.");
        }
    });
});
