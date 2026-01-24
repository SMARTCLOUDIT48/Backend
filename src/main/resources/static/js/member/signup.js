document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("signupForm");

  const imageInput = document.getElementById("imageInput");
  const preview = document.getElementById("preview");
  const plus = document.querySelector(".avatar .plus");

  const nativeLanguage = document.getElementById("nativeLanguage");
  const tos = document.getElementById("tos");
  const privacy = document.getElementById("privacy");

  let isMemberIdChecked = false;

  /* =========================
     아이디 중복 확인
  ========================= */
  document.getElementById("checkIdBtn").addEventListener("click", async () => {
    const memberId = form.memberId.value.trim();
    if (!memberId) {
      alert("아이디를 입력하세요");
      return;
    }

    const res = await fetch(
      `${CONTEXT_PATH}auth/check-member-id?memberId=${memberId}`
    );

    if (!res.ok) {
      alert("아이디 확인 실패");
      return;
    }

    const data = await res.json();

    if (data.available) {
      alert("사용 가능한 아이디입니다");
      isMemberIdChecked = true;
    } else {
      alert("이미 사용 중인 아이디입니다");
      isMemberIdChecked = false;
    }
  });

  // 아이디 변경 시 다시 중복확인 필요
  form.memberId.addEventListener("input", () => {
    isMemberIdChecked = false;
  });

  /* =========================
     이미지 미리보기
  ========================= */
  imageInput.addEventListener("change", () => {
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

    if (!isMemberIdChecked) {
      alert("아이디 중복 확인을 해주세요");
      return;
    }

    const signupData = {
      memberId: form.memberId.value.trim(),
      password: form.password.value,
      nickname: form.nickname.value,
      gender: form.gender.value,
      age: Number(form.age.value),
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

    const res = await fetch(`${CONTEXT_PATH}auth/signup`, {
      method: "POST",
      body: formData
    });

    if (!res.ok) {
      alert("회원가입 실패");
      return;
    }

    alert("회원가입 완료");
    location.href = `${CONTEXT_PATH}login`;
  });
});
