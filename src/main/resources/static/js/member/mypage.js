import { authFetch } from "/js/common/authFetch.js";

/**
 * mypage.js
 * ----------------------------------------
 * - 마이페이지 데이터 로딩
 * - 프로필 이미지 변경
 * - 프로필 수정 모달 (자기소개, 언어레벨)
 */

document.addEventListener("DOMContentLoaded", async () => {

  // ===== DOM =====
  const nicknameEl = document.getElementById("nickname");
  const languageEl = document.getElementById("language");
  const mannerEl = document.getElementById("manner");
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

    nicknameEl.textContent = user.nickname;
    languageEl.textContent = `${user.nativeLanguage} → ${user.levelLanguage}`;
    mannerEl.textContent = user.manner;

    profileImageEl.src =
      user.profileImagePath || "/images/profile/default.png";

    // 모달 초기값 세팅
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
      alert("프로필 수정 완료");
      location.reload();
    } else {
      alert("프로필 수정 실패");
    }
  });
});
