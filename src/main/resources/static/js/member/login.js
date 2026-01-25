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

    const res = await fetch(`${CONTEXT_PATH}api/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ memberId, password })
    });

    const result = await res.json();

    if (result.status !== "SUCCESS") {
      alert(result.message);
      return;
    }

    const { accessToken, refreshToken } = result.data;

    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);

    location.href = `${CONTEXT_PATH}`;
  });
});
