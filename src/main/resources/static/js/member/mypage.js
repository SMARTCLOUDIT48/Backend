import { authFetch } from "/js/common/authFetch.js";
console.log("[mypage.js] loaded");

document.addEventListener("DOMContentLoaded", async () => {

  // ===============================
  // DOM
  // ===============================
  const nicknameEl = document.getElementById("nickname");
  const ageEl = document.getElementById("age");
  const introEl = document.getElementById("intro");
  const nationFlagEl = document.getElementById("nationFlag");
  const nationTextEl = document.getElementById("nationText");
  const levelSpanEl = document.getElementById("levelLanguage"); 

  const profileImageEl = document.getElementById("profileImage");
  const imageInput = document.getElementById("profileImageInput");

  const modal = document.getElementById("profileModal");
  const openModalBtn = document.getElementById("openProfileModal");
  const closeModalBtn = document.getElementById("closeProfileModal");
  const profileForm = document.getElementById("profileForm");

  const introTextarea = profileForm.querySelector('textarea[name="intro"]');
  const levelSelect = profileForm.querySelector('select[name="levelLanguage"]');

  // ===============================
  // 마이페이지 데이터 로딩
  // ===============================
  try {
    const res = await authFetch(`${CONTEXT_PATH}api/members/me`);
    const result = await res.json();

    if (result.status !== "SUCCESS") {
      alert("마이페이지 로딩 실패");
      return;
    }

    const user = result.data;
    console.log("API user =", user);

    // ===== 프로필 =====
    nicknameEl.textContent = user.nickname;
    ageEl.textContent = `(${user.age})`;
    introEl.textContent = user.intro ?? "자기소개를 작성해 주세요.";

    // ===== 일본어 레벨 표시 (🔥 핵심) =====
    levelSpanEl.textContent = user.levelLanguage ?? "BEGINNER";

    // ===== 국적 =====
    if (user.nation === "KOREA") {
      nationFlagEl.textContent = "🇰🇷";
      nationTextEl.textContent = "Korea";
    } else if (user.nation === "JAPAN") {
      nationFlagEl.textContent = "🇯🇵";
      nationTextEl.textContent = "Japan";
    }

    // ===== 이미지 =====
    const imagePath =
      user.profileImagePath && user.profileImageName
        ? `${user.profileImagePath}/${user.profileImageName}`
        : "/images/profile/default.png";

    profileImageEl.src = imagePath;

    // ===== 모달 초기값 =====
    introTextarea.value = user.intro ?? "";
    levelSelect.value = user.levelLanguage;

  } catch (e) {
    console.error(e);
    alert("마이페이지 정보를 불러오지 못했습니다.");
  }

  // ===============================
  // 프로필 이미지 변경
  // ===============================
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

  // ===============================
  // 모달 열고 닫기
  // ===============================
  openModalBtn.addEventListener("click", () => {
    modal.classList.remove("hidden");
  });

  closeModalBtn.addEventListener("click", () => {
    modal.classList.add("hidden");
  });

  // ===============================
  // 프로필 수정 저장
  // ===============================
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
      // 🔥 reload 없이 즉시 반영
      introEl.textContent = introTextarea.value || "자기소개를 작성해 주세요.";
      levelSpanEl.textContent = levelSelect.value;

      modal.classList.add("hidden");
      alert("프로필 수정 완료");
    } else {
      alert("프로필 수정 실패");
    }
  });
});
