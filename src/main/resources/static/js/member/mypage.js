import { authFetch } from "/js/common/authFetch.js";
console.log("[mypage.js] loaded");

document.addEventListener("DOMContentLoaded", async () => {

  /* ===============================
     DOM
  =============================== */
  const nicknameEl = document.getElementById("nickname");
  const ageEl = document.getElementById("age");
  const introEl = document.getElementById("intro");

  const nativeFlagEl = document.getElementById("nativeLang");
  const studyFlagEl = document.getElementById("studyLang");
  const levelTextEl = document.getElementById("levelLanguage");

  const mannerEl = document.getElementById("manner");
  const mannerLineEl = document.querySelector(".manner-line");
  const mannerFillEl = document.querySelector(".manner-fill"); 

  const profileImageEl = document.getElementById("profileImage");
  const imageInput = document.getElementById("profileImageInput");

  const modal = document.getElementById("profileModal");
  const openModalBtn = document.getElementById("openProfileModal");
  const closeModalBtn = document.getElementById("closeProfileModal");
  const profileForm = document.getElementById("profileForm");

  const introTextarea = profileForm.querySelector('textarea[name="intro"]');
  const levelSelect = profileForm.querySelector('select[name="levelLanguage"]');

  /* ===============================
     매너 텍스트 색상 계산
     (왼쪽 파랑 → 오른쪽 빨강)
  =============================== */
  function getMannerTextColor(percent) {
    const p = Math.max(0, Math.min(100, percent)) / 100;

    const start = { r: 50, g: 90, b: 210 }; // blue
    const end   = { r: 255, g: 0,  b: 0 };  // red

    const r = Math.round(start.r + (end.r - start.r) * p);
    const g = Math.round(start.g + (end.g - start.g) * p);
    const b = Math.round(start.b + (end.b - start.b) * p);

    return `rgb(${r}, ${g}, ${b})`;
  }

  /* ===============================
     매너 온도 UI
  =============================== */
  function setMannerTemp(temp) {
    const percent = Math.max(0, Math.min(100, temp));

    // 텍스트
    mannerEl.textContent = `${temp.toFixed(1)}°C`;

    // 🔥 배경 채우기 (핵심)
    mannerFillEl.style.width = `${percent}%`;

    // 🔥 텍스트 색상 = 위치에 맞게
    mannerEl.style.color = getMannerTextColor(percent);
  }

  /* ===============================
     마이페이지 데이터 로딩
  =============================== */
  try {
    const res = await authFetch(`${CONTEXT_PATH}api/members/me`);
    const result = await res.json();

    if (result.status !== "SUCCESS") {
      alert("마이페이지 로딩 실패");
      return;
    }

    const user = result.data;
    console.log("API user =", user);

    /* ===== 기본 정보 ===== */
    nicknameEl.textContent = user.nickname;
    ageEl.textContent = `(${user.age})`;
    introEl.textContent = user.intro ?? "자기소개를 작성해 주세요.";

    /* ===== 언어 ===== */
    const flagMap = {
      KOREAN: "🇰🇷",
      JAPANESE: "🇯🇵"
    };

    nativeFlagEl.textContent = flagMap[user.nativeLanguage] ?? "❓";
    studyFlagEl.textContent = flagMap[user.studyLanguage] ?? "❓";

    /* ===== 레벨 ===== */
    levelTextEl.textContent = user.levelLanguage ?? "BEGINNER";

    /* ===== 매너 온도 ===== */
    const mannerValue = user.manner ?? 36.5;
    setMannerTemp(mannerValue);

    /* ===== 프로필 이미지 ===== */
    const imagePath =
      user.profileImagePath && user.profileImageName
        ? `${user.profileImagePath}/${user.profileImageName}`
        : "/images/profile/default.png";

    profileImageEl.src = imagePath;

    /* ===== 모달 초기값 ===== */
    introTextarea.value = user.intro ?? "";
    levelSelect.value = user.levelLanguage;

  } catch (e) {
    console.error(e);
    alert("마이페이지 정보를 불러오지 못했습니다.");
  }

  /* ===============================
     프로필 이미지 변경
  =============================== */
  imageInput.addEventListener("change", async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("image", file);

    const res = await authFetch(
      `${CONTEXT_PATH}api/members/me/profile-image`,
      {
        method: "PUT",
        body: formData
      }
    );

    if (res.ok) {
      profileImageEl.src = URL.createObjectURL(file);
    } else {
      alert("이미지 변경 실패");
    }
  });

  /* ===============================
     모달 열고 닫기
  =============================== */
  openModalBtn.addEventListener("click", () => {
    modal.classList.remove("hidden");
  });

  closeModalBtn.addEventListener("click", () => {
    modal.classList.add("hidden");
  });

  /* ===============================
     프로필 수정 저장
  =============================== */
  profileForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const formData = new FormData(profileForm);

    const res = await authFetch(
      `${CONTEXT_PATH}api/members/me/profile`,
      {
        method: "PUT",
        body: formData
      }
    );

    if (res.ok) {
      introEl.textContent =
        introTextarea.value || "자기소개를 작성해 주세요.";
      levelTextEl.textContent = levelSelect.value;

      modal.classList.add("hidden");
      alert("프로필 수정 완료");
    } else {
      alert("프로필 수정 실패");
    }
  });

});
