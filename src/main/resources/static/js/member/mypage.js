import { authFetch } from "/js/common/authFetch.js";

document.addEventListener("DOMContentLoaded", async () => {
    const res = await authFetch("/members/me");

    if (!res || !res.ok) return;

    const data = await res.json();

    console.log("내 정보:", data);

    // 예시 렌더링
    document.getElementById("nickname").innerText = data.nickname;
    document.getElementById("nation").innerText = data.nation;
});
