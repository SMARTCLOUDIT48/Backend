import { authFetch } from "/js/common/authFetch.js";
console.log("[userPage.js] loaded (Hybrid Mode)");


document.addEventListener("DOMContentLoaded", async () => {

  // 현재 URL에서 타겟 유저의 memberId 추출 (예: /member/userPage/user123)
  const pathParts = window.location.pathname.split('/');
  const targetMemberId = pathParts[pathParts.length - 1];

  if (!targetMemberId) {
    console.error("대상 유저 ID를 찾을 수 없습니다.");
    return;
  }

  /* ===============================
     1. 이미 HTML에 타임리프로 닉네임, 프사 등 기본 정보가 다 그려졌으므로
        기본 정보를 fetch로 또 가져오는 로직은 과감히 삭제했습니다!
  =============================== */


  /* ===============================
     2. 타겟 유저의 관심사 및 추천 친구 데이터 로드 (이것만 JS가 담당)
  =============================== */
  await loadTargetInterestChips(targetMemberId);
  await loadTargetRecommendList(targetMemberId);

  // (여기에 게시글 수, 댓글 수 등 통계를 가져오는 fetch 함수를 추가하셔도 좋습니다)
 
 
  // 현재 채팅중인 사람수
await loadTargetChatActivity(TARGET_USER_ID);
  
  /* ===============================
     3. 좋아요/싫어요 이벤트
  =============================== */
  const likeBtn = document.getElementById("likeBtn");
  const dislikeBtn = document.getElementById("dislikeBtn");

  if (likeBtn) {
      likeBtn.addEventListener("click", async () => {
          console.log(`${targetMemberId}님에게 좋아요 클릭! (API 연동 필요)`);
      });
  }

  if (dislikeBtn) {
      dislikeBtn.addEventListener("click", async () => {
          console.log(`${targetMemberId}님에게 싫어요 클릭! (API 연동 필요)`);
      });
  }
});

/* ===============================
   타겟 유저 관심사 로드
=============================== */
async function loadTargetInterestChips(memberId) {
  try {
    // 🚨 [주의] 백엔드에 이 경로의 @RestController API가 있어야 합니다!
    const res = await authFetch(`${CONTEXT_PATH}api/members/${memberId}/interests`);
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
   타겟 유저 기준 추천 친구 로드
=============================== */
async function loadTargetRecommendList(memberId) {
  const wrap = document.getElementById("recommendGrid");
  if (!wrap) return;

  try {
    // 🚨 [주의] 백엔드에 이 경로의 @RestController API가 있어야 합니다!
    const res = await authFetch(`${CONTEXT_PATH}api/recommend/${memberId}`);
    if (!res.ok) {
      wrap.innerHTML = `<p class="muted">추천 불러오기 실패</p>`;
      return;
    }

    const list = await res.json();
    wrap.innerHTML = "";

    if (!list || list.length === 0) {
      wrap.innerHTML = `<p class="muted">추천 결과가 없습니다.</p>`;
      return;
    }

    list.slice(0, 4).forEach(user => {
      const imagePath = user.profileImagePath && user.profileImageName
          ? `${user.profileImagePath}/${user.profileImageName}`
          : "/images/profile/default.png";

      const interests = user.interests ?? [];
      const visibleInterests = interests.slice(0, 3);

      let interestsHtml = visibleInterests
        .map(type => `<span class="tag">${convertInterestType(type)}</span>`)
        .join("");

      if (interests.length > 3) {
        const extraCount = interests.length - 3;
        interestsHtml += `<span class="tag more">+${extraCount}</span>`;
      }

      const item = document.createElement("article");
      item.className = "reco";

      item.innerHTML = `
        <div class="reco-top">
          <div class="reco-avatar">
            <img src="${imagePath}" style="width:100%; height:100%; object-fit:cover; border-radius:50%;">
          </div>
          <div class="reco-info">
            <strong>${user.nickname}</strong>
            <span class="flag">${getFlag(user.nation)}</span>
            <div class="reco-sub-row">
              <div class="lang">
                ${getLanguageFlag(user.nativeLanguage)} → ${getLanguageFlag(user.studyLanguage)}
              </div>
              <div class="stars">${renderLevelStars(user.levelLanguage)}</div>
            </div>
            <div class="match">매칭 ${user.matchPoint ?? 0}%</div>
          </div>
        </div>
        <div class="reco-tags">
          ${interestsHtml || `<span class="tag empty">관심사 없음</span>`}
        </div>
      `;

      item.addEventListener("click", () => {
        location.href = `${CONTEXT_PATH}member/userPage/${user.id}`;
      });

      wrap.appendChild(item);
    });

  } catch (e) {
    console.error(e);
    wrap.innerHTML = `<p class="muted">오류 발생</p>`;
  }
}

/* ===============================
   유틸리티 함수 모음
=============================== */
const INTEREST_DETAIL_LABEL = {
  MOVIE: "영화", DRAMA: "드라마", MUSIC: "음악", EXHIBITION: "전시·미술관", PERFORMANCE: "공연·연극",
  PHOTO: "사진", GAME: "게임", BOARD_GAME: "보드게임", DIY: "DIY·만들기", COLLECT: "수집",
  FITNESS: "헬스·피트니스", RUNNING: "러닝·조깅", YOGA: "요가·필라테스", BALL_SPORTS: "구기 스포츠", HIKING: "등산·하이킹",
  DOMESTIC_TRAVEL: "국내 여행", OVERSEAS_TRAVEL: "해외 여행", BACKPACKING: "배낭여행", FOOD_TRIP: "맛집 탐방", LOCAL_TOUR: "지역 산책·로컬 투어",
  COOKING: "요리", BAKING: "베이킹", CAFE: "카페 투어", ALCOHOL: "술·와인", GOURMET: "미식 탐방",
  LANGUAGE_STUDY: "언어 학습", CERTIFICATE: "자격증 준비", READING: "독서", STUDY_GROUP: "스터디 모임", CAREER: "커리어 개발",
  PROGRAMMING: "프로그래밍", WEB_APP: "웹·앱 개발", GAME_DEV: "게임 개발", AI_DATA: "AI·데이터", IT_TREND: "IT 트렌드",
  DAILY: "일상 공유", PET: "반려동물", FASHION: "패션", INTERIOR: "인테리어", WELLNESS: "건강·웰빙"
};

function convertInterestToLabel(item) {
  return INTEREST_DETAIL_LABEL[item.interestDetail] ?? INTEREST_DETAIL_LABEL[item.interest] ?? "알 수 없음";
}

function convertInterestType(type) {
  const map = {
    CULTURE: "문화·예술", HOBBY: "취미·여가", SPORTS: "운동·스포츠", TRAVEL: "여행·지역",
    FOOD: "음식·요리", STUDY: "학습·자기계발", IT: "IT·기술", LIFESTYLE: "라이프스타일"
  };
  return map[type] ?? type;
}

function renderLevelStars(level) {
  const levelMap = { BEGINNER: 1, INTERMEDIATE: 2, ADVANCED: 3, NATIVE: 4 };
  const score = levelMap[level] ?? 0;
  let stars = "";
  for (let i = 0; i < 4; i++) { stars += i < score ? "★" : "☆"; }
  return stars;
}

function getFlag(nation) {
  const map = { KOREA: "🇰🇷", JAPAN: "🇯🇵" };
  return map[nation] ?? "";
}

function getLanguageFlag(lang) {
  const map = { KOREAN: "🇰🇷", JAPANESE: "🇯🇵" };
  return map[lang] ?? "❓";
}


/* ===============================
   💌 채팅 활동량 로드
=============================== */
async function loadTargetChatActivity(userId) {
  try {

    const res = await authFetch(`${CONTEXT_PATH}chat/activity/${userId}`);


    if (!res.ok) {
      console.error("API 호출 실패");
      return;
    }

    const count = await res.json();

    const countEl = document.getElementById("chattingCount");
    const hotLevelEl = document.getElementById("hotLevel");

    if (countEl) countEl.textContent = count;
    if (!hotLevelEl) return;

    if (count === 0) {
  hotLevelEl.textContent = "지금 대화하면 칼답 가능성! ✨";

  hotLevelEl.style.color = "#6e7b8f";
}
else if (count <= 4) {
  hotLevelEl.textContent = "오늘 대화 분위기가 좋은 분이네요 💬";
  hotLevelEl.style.color = "#ff9f1c";
}
else if (count <= 10) {
  hotLevelEl.textContent = "인기멤버에요 🔥!";
  hotLevelEl.style.color = "#ff4d4f";
}
else {
  hotLevelEl.textContent = "인플루언서급이에요! 👑";
  hotLevelEl.style.color = "#d4af37";
  hotLevelEl.style.fontWeight = "900";
}

  } catch (err) {
    console.error("❌ 타겟 활동량 조회 실패:", err);
  }
}