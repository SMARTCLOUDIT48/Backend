console.log(CONTEXT_PATH);

document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("signupForm");
  if (!form) return;


  /* =========================
     DOM 요소
  ========================= */
  const imageInput = document.getElementById("imageInput");
  const preview = document.getElementById("preview");
  const plus = document.querySelector(".avatar .plus");

  const memberIdInput = document.getElementById("memberId");
  const nicknameInput = document.getElementById("nickname");

  const studyLanguage = document.getElementById("studyLanguage");

  const tos = document.getElementById("tos");
  const privacy = document.getElementById("privacy");

  /* =========================
     상태 플래그
  ========================= */
  let isMemberIdChecked = false;
  let isNicknameChecked = false;

  /* =========================
     아이디 중복 확인
  ========================= */
  document.getElementById("checkIdBtn")?.addEventListener("click", async () => {
    const memberId = memberIdInput.value.trim();
    if (!memberId) {
      alert("아이디를 입력하세요");
      return;
    }

    try {
      const res = await fetch(
        `${CONTEXT_PATH}api/members/exists?memberId=${encodeURIComponent(memberId)}`
      );
      const result = await res.json();


      if (res.ok && result.data?.available) {
        alert("사용 가능한 아이디입니다");
        isMemberIdChecked = true;
      } else {
        alert("이미 사용 중인 아이디입니다");
        isMemberIdChecked = false;
      }
    } catch (e) {
      alert("아이디 확인 중 오류가 발생했습니다.");
      isMemberIdChecked = false;
    }
  });

  memberIdInput.addEventListener("input", () => {
    isMemberIdChecked = false;
  });

  /* =========================
     닉네임 중복 확인
  ========================= */
  document.getElementById("checkNicknameBtn")?.addEventListener("click", async () => {
    const nickname = nicknameInput.value.trim();
    if (!nickname) {
      alert("닉네임을 입력하세요");
      return;
    }

    try {
      const res = await fetch(
        `${CONTEXT_PATH}api/members/exists?nickname=${encodeURIComponent(nickname)}`
      );
      const result = await res.json();

      if (res.ok && result.data?.available) {
        alert("사용 가능한 닉네임입니다");
        isNicknameChecked = true;
      } else {
        alert("이미 사용 중인 닉네임입니다");
        isNicknameChecked = false;
      }
    } catch (e) {
      alert("닉네임 확인 중 오류가 발생했습니다.");
      isNicknameChecked = false;
    }
  });

  nicknameInput.addEventListener("input", () => {
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

    /* 비밀번호 확인 */
    if (form.password.value !== form.passwordConfirm.value) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    /* 약관 동의 */
    if (!tos.checked || !privacy.checked) {
      alert("필수 약관에 동의해주세요.");
      return;
    }

    /* 중복 확인 여부 */
    if (!isMemberIdChecked || !isNicknameChecked) {
      alert("아이디와 닉네임 중복 확인을 완료해주세요.");
      return;
    }

    /* 나이 검증 */
    const age = Number(form.age.value);
    if (Number.isNaN(age) || age <= 0) {
      alert("나이를 올바르게 입력해주세요.");
      return;
    }

    /* 학습 언어 선택 여부 */
    if (!studyLanguage.value) {
      alert("학습 언어를 선택해주세요.");
      return;
    }

    /* 전송 데이터 */
    const signupData = {
      memberId: memberIdInput.value.trim(),
      password: form.password.value,
      nickname: nicknameInput.value.trim(),
      gender: form.gender.value,
      age,
      nation: form.nation.value,
      studyLanguage: studyLanguage.value,
      levelLanguage: form.levelLanguage.value
    };

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
      const res = await fetch(`${CONTEXT_PATH}api/members`, {
        method: "POST",
        body: formData,
        credentials: "include"
      });

      const result = await res.json();

      if (!res.ok || result.status !== "SUCCESS") {
        alert(result.message ?? "회원가입에 실패했습니다.");
        return;
      }

      alert("회원가입 완료");
      location.href = `${CONTEXT_PATH}member/interest`;
    } catch (e) {
      alert("회원가입 중 오류가 발생했습니다.");
    }
  });
});
