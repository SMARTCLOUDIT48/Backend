import { authFetch } from "/js/common/authFetch.js";
console.log("[userPage.js] loaded (Hybrid Mode)");

let currentReaction = null;

document.addEventListener("DOMContentLoaded", async () => {
    // 페이지 로드 시 관심사 불러오기 실행
    await loadInterestChips();
    await loadTargetChatActivity(PAGE_USER_ID);
    await loadReactionStatus();

    // 방문 기록
    recordProfileView();

    const likeBtn = document.getElementById("likeBtn");
        if (likeBtn) {
            likeBtn.addEventListener("click", () => handleUserReaction("LIKE"));
        }

    const dislikeBtn = document.getElementById("dislikeBtn");
        if (dislikeBtn) {
            dislikeBtn.addEventListener("click", () => handleUserReaction("DISLIKE"));
        }
});


// 방문 기록 남기기
async function recordProfileView() {
    try {
        // 우리가 보고 있는 페이지 주인의 숫자 ID (PAGE_USER_ID)를 주소에 넣어서 POST 요청!
        const res = await authFetch(`${CONTEXT_PATH}api/profile-views/${PAGE_USER_ID}`, {
            method: 'POST'
        });

        // 결과 확인 (디버깅용 - 나중에 지우셔도 됩니다)
        if (res.ok) {
            console.log(`[방문 기록 성공] 대상 유저 ID: ${PAGE_USER_ID}`);
        } else if (res.status === 401) {
            console.log("로그인하지 않은 유저의 방문이므로 기록하지 않습니다.");
        } else {
            console.error("방문 기록 실패:", res.status);
        }
    } catch (e) {
        console.error("방문 기록 전송 중 에러:", e);
    }
}


/* ===============================
   관심사 로드 (특정 유저용)
=============================== */
async function loadInterestChips() {
  try {
      const res = await authFetch(`${CONTEXT_PATH}api/member/userPage/${PAGE_MEMBER_ID}/interests`);

      if (!res.ok) {
          console.error("관심사 API 실패:", res.status);
          renderInterestChips([]);
          return;
      }

      const result = await res.json();
      console.log("관심사 응답 데이터:", result); // F12 콘솔에서 데이터가 잘 오는지 확인용!

      const interests = result.data !== undefined ? result.data : (Array.isArray(result) ? result : []);
      renderInterestChips(interests);
    } catch (e) {
      console.error("관심사 에러:", e);
      renderInterestChips([]);
    }
}

function renderInterestChips(interests) {
  const wrap = document.getElementById("interestChips");
    if (!wrap) return;

    wrap.innerHTML = "";

    if (!interests || interests.length === 0) {
      wrap.innerHTML = `<span style="font-size:13px; color:#888; font-weight:600;">등록된 관심사가 없습니다.</span>`;
      return;
    }

    interests.forEach(item => {
      const chip = document.createElement("span");
      chip.className = "chip";
      chip.textContent = convertInterestToLabel(item);
      wrap.appendChild(chip);
    });
}

async function handleUserReaction(reactionType) {
    try {
        // 1. 백엔드 컨트롤러가 @RequestParam으로 받으므로, 쿼리 파라미터 형태로 만들어줍니다.
        const params = new URLSearchParams({
            toUserId: PAGE_USER_ID,    // 우리가 HTML 상단에 선언해둔 대상 유저의 PK 숫자!
            reaction: reactionType     // 'LIKE' 또는 'DISLIKE'
        });

        // 2. POST 요청 전송
        const res = await authFetch(`${CONTEXT_PATH}api/reactions?${params.toString()}`, {
            method: 'POST'
        });

        // 3. 비로그인 예외 처리
        if (res.status === 401) {
            if (confirm("로그인이 필요한 기능입니다. 로그인 페이지로 이동하시겠습니까?")) {
                location.href = '/login'; // 로그인 경로에 맞게 수정 가능
            }
            return;
        }

        // 4. 중복 클릭(이미 좋아요를 누름) 등 백엔드 에러 발생 시
        if (!res.ok) {
            const errorData = await res.json().catch(() => ({}));
            alert(errorData.message || "이미 반응을 남기셨거나 처리 중 오류가 발생했습니다.");
            return;
        }

        // 5. 서버 처리가 성공했다면 화면의 숫자와 온도계를 부드럽게 업데이트!
        updateReactionUI(reactionType);

    } catch (error) {
        console.error("반응 전송 중 에러:", error);
        alert("서버와 통신 중 문제가 발생했습니다.");
    }
}

function updateReactionUI(clickedReaction) {
    const likeCountSpan = document.getElementById("likeCount1");
    const mannerSpan = document.getElementById("manner");
    const mannerFill = document.querySelector(".manner-fill");

    const likeBtn = document.getElementById("likeBtn");
    const dislikeBtn = document.getElementById("dislikeBtn");

    let currentLikes = parseInt(likeCountSpan.innerText) || 0;
    let currentTemp = parseFloat(mannerSpan.innerText.replace("°C", "")) || 36.5;

    if (clickedReaction === "LIKE") {
        if (currentReaction === "LIKE") {
            currentLikes -= 1;
            currentTemp -= 0.1;
            currentReaction = null;
            likeBtn.classList.remove("active-like");
        }
        else if (currentReaction === "DISLIKE") {
            currentLikes += 1;
            currentTemp += 0.2;
            currentReaction = "LIKE";
            dislikeBtn.classList.remove("active-dislike");
            likeBtn.classList.add("active-like");
        }
        else {
            currentLikes += 1;
            currentTemp += 0.1;
            currentReaction = "LIKE";
            likeBtn.classList.add("active-like");
        }
    }
    else if (clickedReaction === "DISLIKE") {
        if (currentReaction === "DISLIKE") {
            currentTemp += 0.1;
            currentReaction = null;
            dislikeBtn.classList.remove("active-dislike");
        }
        else if (currentReaction === "LIKE") {
            currentLikes -= 1;
            currentTemp -= 0.2;
            currentReaction = "DISLIKE";
            likeBtn.classList.remove("active-like");
            dislikeBtn.classList.add("active-dislike");
        }
        else {
            currentTemp -= 0.1;
            currentReaction = "DISLIKE";
            dislikeBtn.classList.add("active-dislike");
        }
    }

    if (likeCountSpan) likeCountSpan.innerText = currentLikes;
    if (mannerSpan) {
        const newTemp = currentTemp.toFixed(1);
        mannerSpan.innerText = newTemp + "°C";
        if (mannerFill) mannerFill.style.width = newTemp + "%";
    }
}

async function loadReactionStatus() {
    try {
        const res = await authFetch(`${CONTEXT_PATH}api/reactions/status?toUserId=${PAGE_USER_ID}`);
        if (!res.ok) return;

        const result = await res.json();
        const savedStatus = result.data; // "LIKE", "DISLIKE", 또는 null

        const likeBtn = document.getElementById("likeBtn");
        const dislikeBtn = document.getElementById("dislikeBtn");

        // DB에서 가져온 상태에 맞춰 버튼 색상 활성화 및 변수 셋팅
        if (savedStatus === "LIKE") {
            currentReaction = "LIKE";
            if (likeBtn) likeBtn.classList.add("active-like");
        } else if (savedStatus === "DISLIKE") {
            currentReaction = "DISLIKE";
            if (dislikeBtn) dislikeBtn.classList.add("active-dislike");
        }
    } catch (e) {
        console.error("반응 상태 로드 에러:", e);
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
  const key = typeof item === 'object' ? (item.interestDetail || item.interest) : item;
  return INTEREST_DETAIL_LABEL[key] ?? "알 수 없음";
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
      // 이제 userId로 정확히 숫자(예: 1, 2)가 들어갑니다!
      const res = await authFetch(`${CONTEXT_PATH}chat/activity/${userId}`);

      if (!res.ok) {
        console.error("채팅 API 호출 실패");
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
      } else if (count <= 4) {
        hotLevelEl.textContent = "오늘 대화 분위기가 좋은 분이네요 💬";
        hotLevelEl.style.color = "#ff9f1c";
      } else if (count <= 10) {
        hotLevelEl.textContent = "인기멤버에요! 🔥";
        hotLevelEl.style.color = "#ff4d4f";
      } else {
        hotLevelEl.textContent = "인플루언서급이에요! 👑";
        hotLevelEl.style.color = "#d4af37";
        hotLevelEl.style.fontWeight = "900";
      }

    } catch (err) {
      console.error("❌ 타겟 활동량 조회 실패:", err);
    }
}