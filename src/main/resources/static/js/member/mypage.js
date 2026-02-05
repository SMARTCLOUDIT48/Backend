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
     매너 온도 색상
  =============================== */
  function getMannerTextColor(percent) {
    const p = Math.max(0, Math.min(100, percent)) / 100;
    const start = { r: 50, g: 90, b: 210 };
    const end   = { r: 255, g: 0, b: 0 };
    const r = Math.round(start.r + (end.r - start.r) * p);
    const g = Math.round(start.g + (end.g - start.g) * p);
    const b = Math.round(start.b + (end.b - start.b) * p);
    return `rgb(${r}, ${g}, ${b})`;
  }

  function setMannerTemp(temp) {
    const percent = Math.max(0, Math.min(100, temp));
    mannerEl.textContent = `${temp.toFixed(1)}°C`;
    mannerFillEl.style.width = `${percent}%`;
    mannerEl.style.color = getMannerTextColor(percent);
  }

  /* ===============================
     마이페이지 정보 로드
  =============================== */
  try {
    const res = await authFetch(`${CONTEXT_PATH}api/members/me`);
    const result = await res.json();
    if (result.status !== "SUCCESS") return;

    const user = result.data;

    nicknameEl.textContent = user.nickname;
    ageEl.textContent = `(${user.age})`;
    introEl.textContent = user.intro ?? "자기소개를 작성해 주세요.";

    const flagMap = { KOREAN: "🇰🇷", JAPANESE: "🇯🇵" };
    nativeFlagEl.textContent = flagMap[user.nativeLanguage] ?? "❓";
    studyFlagEl.textContent = flagMap[user.studyLanguage] ?? "❓";

    levelTextEl.textContent = user.levelLanguage ?? "BEGINNER";
    setMannerTemp(user.manner ?? 36.5);

    const imagePath =
      user.profileImagePath && user.profileImageName
        ? `${user.profileImagePath}/${user.profileImageName}`
        : "/images/profile/default.png";
    profileImageEl.src = imagePath;

    introTextarea.value = user.intro ?? "";
    levelSelect.value = user.levelLanguage ?? "";

  } catch (e) {
    console.error(e);
  }

  /* ===============================
     관심사 / 좋아요 로드
  =============================== */
  await loadInterestChips();
  await loadLikedMeList();

  window.addEventListener("interest:updated", loadInterestChips);

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
      { method: "PUT", body: formData }
    );

    if (res.ok) {
      profileImageEl.src = URL.createObjectURL(file);
    }
  });

  /* ===============================
     모달 제어
  =============================== */
  openModalBtn.addEventListener("click", () => modal.classList.remove("hidden"));
  closeModalBtn.addEventListener("click", () => modal.classList.add("hidden"));

  /* ===============================
     프로필 수정
  =============================== */
  profileForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const res = await authFetch(
      `${CONTEXT_PATH}api/members/me/profile`,
      { method: "PUT", body: new FormData(profileForm) }
    );

    if (res.ok) {
      introEl.textContent = introTextarea.value || "자기소개를 작성해 주세요.";
      levelTextEl.textContent = levelSelect.value;
      modal.classList.add("hidden");
      alert("프로필 수정 완료");
    }
  });
});

/* ===============================
   관심사 로드
=============================== */
async function loadInterestChips() {
  try {
    const res = await authFetch(`${CONTEXT_PATH}api/members/me/interests`);
    if (!res.ok) return;

    const result = await res.json();
    renderInterestChips(result.data ?? []);
  } catch (e) {
    console.error(e);
  }
}

function renderInterestChips(interests) {
  const wrap = document.getElementById("interestChips");
  if (!wrap) return;

  wrap.innerHTML = "";

  if (interests.length === 0) {
    wrap.innerHTML = `<span class="chip empty">관심사 없음</span>`;
    return;
  }

  interests.forEach(item => {
    const chip = document.createElement("span");
    chip.className = "chip";
    chip.textContent = convertInterestToLabel(item);
    wrap.appendChild(chip);
  });
}

/* ===============================
   나를 좋아요한 사람
=============================== */
async function loadLikedMeList() {
  const wrap = document.getElementById("likedMeList");
  if (!wrap) return;

  try {
    const res = await authFetch(`${CONTEXT_PATH}api/reactions/liked-me`);
    if (!res.ok) {
      wrap.innerHTML = `<p class="muted">불러오기 실패</p>`;
      return;
    }

    const result = await res.json();
    const list = result.data ?? [];
    wrap.innerHTML = "";

    if (list.length === 0) {
      wrap.innerHTML = `<p class="muted">아직 좋아요가 없습니다.</p>`;
      return;
    }

    list.forEach(user => {
      const imgSrc = user.profileImagePath && user.profileImageName
        ? `${user.profileImagePath}/${user.profileImageName}`
        : "/images/profile/default.png";

      const item = document.createElement("div");
      item.className = "viewer-item";
      item.innerHTML = `
        <div class="viewer-avatar">
          <img src="${imgSrc}" alt="profile">
        </div>
        <div class="viewer-info">
          <strong>${user.nickname}</strong>
          <span class="viewer-time">${formatTime(user.likedAt)}</span>
        </div>
        <button class="btn-view" data-user-id="${user.userId}">프로필</button>
      `;
      wrap.appendChild(item);
    });

  } catch (e) {
    console.error(e);
    wrap.innerHTML = `<p class="muted">오류 발생</p>`;
  }
}

/* ===============================
   시간 포맷
=============================== */
function formatTime(isoString) {
  if (!isoString) return "";
  const diff = (Date.now() - new Date(isoString)) / 1000;
  if (diff < 60) return "방금 전";
  if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;
  return `${Math.floor(diff / 86400)}일 전`;
}

/* ===============================
   프로필 이동
=============================== */
document.addEventListener("click", (e) => {
  const btn = e.target.closest(".btn-view");
  if (!btn) return;

  const userId = btn.dataset.userId;
  if (userId) {
    location.href = `${CONTEXT_PATH}members/${userId}`;
  }
});

/* ===============================
   enum → 한글
=============================== */
const INTEREST_DETAIL_LABEL = {
  MOVIE: "영화",
  DRAMA: "드라마",
  MUSIC: "음악",
  EXHIBITION: "전시·미술관",
  PERFORMANCE: "공연·연극",
  PHOTO: "사진",
  GAME: "게임",
  BOARD_GAME: "보드게임",
  DIY: "DIY·만들기",
  COLLECT: "수집",
  FITNESS: "헬스·피트니스",
  RUNNING: "러닝·조깅",
  YOGA: "요가·필라테스",
  BALL_SPORTS: "구기 스포츠",
  HIKING: "등산·하이킹",
  DOMESTIC_TRAVEL: "국내 여행",
  OVERSEAS_TRAVEL: "해외 여행",
  BACKPACKING: "배낭여행",
  FOOD_TRIP: "맛집 탐방",
  LOCAL_TOUR: "지역 산책·로컬 투어",
  COOKING: "요리",
  BAKING: "베이킹",
  CAFE: "카페 투어",
  ALCOHOL: "술·와인",
  GOURMET: "미식 탐방",
  LANGUAGE_STUDY: "언어 학습",
  CERTIFICATE: "자격증 준비",
  READING: "독서",
  STUDY_GROUP: "스터디 모임",
  CAREER: "커리어 개발",
  PROGRAMMING: "프로그래밍",
  WEB_APP: "웹·앱 개발",
  GAME_DEV: "게임 개발",
  AI_DATA: "AI·데이터",
  IT_TREND: "IT 트렌드",
  DAILY: "일상 공유",
  PET: "반려동물",
  FASHION: "패션",
  INTERIOR: "인테리어",
  WELLNESS: "건강·웰빙"
};

function convertInterestToLabel(item) {
  return INTEREST_DETAIL_LABEL[item.interestDetail]
      ?? INTEREST_DETAIL_LABEL[item.interest]
      ?? "알 수 없음";
}
