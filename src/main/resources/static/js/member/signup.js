document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("signupForm");
  if (!form) return;

  const imageInput = document.getElementById("imageInput");
  const preview = document.getElementById("preview");
  const plus = document.querySelector(".avatar .plus");

  const nativeLanguage = document.getElementById("nativeLanguage");
  const tos = document.getElementById("tos");
  const privacy = document.getElementById("privacy");

  let isMemberIdChecked = false;
  let isNicknameChecked = false;

  /* =========================
     아이디 중복 확인
  ========================= */
  document.getElementById("checkIdBtn")?.addEventListener("click", async () => {
    const memberId = form.memberId.value.trim();
    if (!memberId) {
      alert("아이디를 입력하세요");
      return;
    }

    try {
      const res = await fetch(
        `${CONTEXT_PATH}api/members/exists?memberId=${encodeURIComponent(memberId)}`
      );
      const result = await res.json();

      if (result.status === "SUCCESS" && result.data.available) {
        alert("사용 가능한 아이디입니다");
        isMemberIdChecked = true;
      } else {
        alert(result.message);
        isMemberIdChecked = false;
      }
    } catch {
      alert("아이디 확인 중 오류가 발생했습니다.");
    }
  });

  form.memberId.addEventListener("input", () => {
    isMemberIdChecked = false;
  });

  /* =========================
     닉네임 중복 확인
  ========================= */
  document.getElementById("checkNicknameBtn")?.addEventListener("click", async () => {
    const nickname = form.nickname.value.trim();
    if (!nickname) {
      alert("닉네임을 입력하세요");
      return;
    }

    try {
      const res = await fetch(
        `${CONTEXT_PATH}api/members/exists?nickname=${encodeURIComponent(nickname)}`
      );
      const result = await res.json();

      if (result.status === "SUCCESS" && result.data.available) {
        alert("사용 가능한 닉네임입니다");
        isNicknameChecked = true;
      } else {
        alert(result.message);
        isNicknameChecked = false;
      }
    } catch {
      alert("닉네임 확인 중 오류가 발생했습니다.");
    }
  });

  form.nickname.addEventListener("input", () => {
    isNicknameChecked = false;
  });

  /* =========================
     이미지 미리보기
  ========================= */
  imageInput?.addEventListener("change", () => {
    const file = imageInput.files[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      alert("이미지 파일만 업로드 가능합니다.");
      imageInput.value = "";
      return;
    }

    preview.src = URL.createObjectURL(file);
    preview.style.display = "block";
    if (plus) plus.style.display = "none";
  });

  /* =========================
     회원가입 submit
  ========================= */
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    if (form.password.value !== form.passwordConfirm.value) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    if (!tos.checked || !privacy.checked) {
      alert("필수 약관에 동의해주세요.");
      return;
    }

    if (!isMemberIdChecked || !isNicknameChecked) {
      alert("중복 확인을 완료해주세요.");
      return;
    }

    const age = Number(form.age.value);
    if (Number.isNaN(age)) {
      alert("나이를 올바르게 입력해주세요.");
      return;
    }

    const signupData = {
      memberId: form.memberId.value.trim(),
      password: form.password.value,
      nickname: form.nickname.value.trim(),
      gender: form.gender.value,
      age,
      nation: form.nation.value,
      nativeLanguage: nativeLanguage.value,
      levelLanguage: form.levelLanguage.value
    };

    const formData = new FormData();
    formData.append(
      "data",
      new Blob([JSON.stringify(signupData)], { type: "application/json" })
    );

    if (imageInput.files.length > 0) {
      formData.append("image", imageInput.files[0]);
    }

    try {
      const res = await fetch(`${CONTEXT_PATH}api/members`, {
        method: "POST",
        body: formData
      });

      const result = await res.json();

      if (result.status !== "SUCCESS") {
        alert(result.message);
        return;
      }

      alert("회원가입 완료");
      location.href = `${CONTEXT_PATH}login`;

    } catch {
      alert("회원가입 중 오류가 발생했습니다.");
    }
  });
});
