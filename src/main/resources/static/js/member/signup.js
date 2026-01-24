document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("signupForm");
    const imageInput = document.getElementById("imageInput");
    const nativeLanguage = document.getElementById("nativeLanguage");
    const tos = document.getElementById("tos");
    const privacy = document.getElementById("privacy");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        // ===== 기본 검증 =====
        if (form.password.value !== form.passwordConfirm.value) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        if (!tos.checked || !privacy.checked) {
            alert("약관에 동의해주세요.");
            return;
        }

   
        if (!form.gender.value) {
            alert("성별을 선택해주세요.");
            return;
        }

        if (!form.nation.value) {
            alert("국가를 선택해주세요.");
            return;
        }

        if (!nativeLanguage.value) {
            alert("학습 언어를 선택해주세요.");
            return;
        }

        if (!form.levelLanguage.value) {
            alert("언어 레벨을 선택해주세요.");
            return;
        }

        // ===== 전송 데이터 =====
        const signupData = {
            memberId: form.memberId.value.trim(),
            password: form.password.value,
            nickname: form.nickname.value,
            gender: form.gender.value,              // MALE / FEMALE
            age: Number(form.age.value),
            nation: form.nation.value,              // KOREA / JAPAN
            nativeLanguage: nativeLanguage.value,   // KOREAN / JAPANESE
            levelLanguage: form.levelLanguage.value // BEGINNER / ...
        };

        // ===== multipart/form-data 구성 =====
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

        // ===== 요청 =====
        const res = await fetch("/auth/signup", {
            method: "POST",
            body: formData
        });

        if (!res.ok) {
            alert("회원가입 실패");
            return;
        }

        alert("회원가입 완료");
        location.href = "/login";
    });
});
