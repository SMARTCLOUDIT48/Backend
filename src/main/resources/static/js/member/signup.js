document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("signupForm");
    const imageInput = document.getElementById("imageInput");
    const preview = document.getElementById("preview");

    /* =========================
       이미지 미리보기
    ========================= */
    imageInput.addEventListener("change", () => {
        const file = imageInput.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = e => {
            preview.src = e.target.result;
            preview.style.display = "block";
        };
        reader.readAsDataURL(file);
    });

    /* =========================
       회원가입 submit
    ========================= */
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        // 비밀번호 확인
        const password = form.password.value;
        const passwordConfirm = form.passwordConfirm.value;

        if (password !== passwordConfirm) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        // 약관 동의 체크
        if (!document.getElementById("tos").checked ||
            !document.getElementById("privacy").checked) {
            alert("필수 약관에 동의해주세요.");
            return;
        }

        // DTO 데이터 구성
        const signupData = {
            email: form.memberId.value,
            password: password,
            nickname: form.nickname.value,
            gender: form.gender.value,
            age: Number(form.age.value),
            nation: form.nation.value,
            nativeLanguage: document.getElementById("nativeLanguage").value,
            levelLanguage: form.levelLanguage.value
        };

        // multipart/form-data 생성
        const formData = new FormData();
        formData.append(
            "data",
            new Blob([JSON.stringify(signupData)], {
                type: "application/json"
            })
        );

        if (imageInput.files.length > 0) {
            formData.append("image", imageInput.files[0]);
        }

        try {
            const res = await fetch("/auth/signup", {
                method: "POST",
                body: formData
            });

            if (!res.ok) {
                const err = await res.json();
                alert(err.message || "회원가입 실패");
                return;
            }

            alert("회원가입 완료!\n이메일 인증 후 로그인해주세요.");
            window.location.href = "/login";

        } catch (err) {
            console.error(err);
            alert("서버 오류가 발생했습니다.");
        }
    });
});
