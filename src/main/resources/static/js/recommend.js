window.addEventListener('DOMContentLoaded', () => {
    loadRecommend();

    const ageMin = document.getElementById("ageMin");
    const ageMax = document.getElementById("ageMax");
    const ageMinValue = document.getElementById("ageMinValue");
    const ageMaxValue = document.getElementById("ageMaxValue");
    const ageFill = document.getElementById("ageFill");

    const fd = new FormData(document.getElementById('filterForm'));
    const levels = fd.getAll('levels'); // ["1","3","4"] 처럼 복수
    // 아무것도 체크 안 된 경우 levels.length === 0  → 서버에서 ALL 처리
    const lv = levels.length ? levels.join(',') : '1,2,3,4';

    ageMin.addEventListener("input", syncAge);
    ageMax.addEventListener("input", syncAge);

    syncAge();
    getSelectedInterests();

    document.getElementById('filterForm')?.addEventListener('reset', () => {
        setTimeout(() => {
            syncAge();
        }, 0);
    });


    //채팅 신청 버튼으로 채팅방 입장
    document.getElementById('recommendTbody').addEventListener('click', async (e) => {
        const chat_partner_btn = e.target.closest('.chat-btn');
        if (!chat_partner_btn) return;

        const partnerId = chat_partner_btn.dataset.userId;
        chat_partner_btn.disabled = true;

        try {
            const res = await fetch(`/api/chat/rooms/direct/${partnerId}`, { method: 'POST' });
            if (!res.ok) throw new Error('HTTP ' + res.status);

            const data = await res.json();
            // 채팅 페이지 라우팅 규칙에 맞게 수정
            // ✅ roomId를 임시 저장하고 /chat으로 이동 (URL은 /chat 그대로)
            sessionStorage.setItem('openRoomId', data.roomId);

            location.href = `/chat`;

        } catch (err) {
            console.error(err);
            alert('채팅방 생성 실패');
            chat_partner_btn.disabled = false;
        }
    });

    const mathch_btn = document.getElementById('auto_matching');

    mathch_btn.addEventListener('click', async () => {
        mathch_btn.disabled = true;

        try {
            // ✅ 필요하면 여기서 필터 조건을 같이 보냅니다.
            // 서버에서 로그인 유저 기준으로 자동 계산한다면 body 없이 보내도 됩니다.
            const payload = {
                // 예시(선택): 서버가 criteriaKey를 요구한다면 전달
                // criteriaKey: "g=FEMALE|n=JAPAN|s=KOREAN|l=ANY"
                criteriaKey: buildCriteriaKey() //임시 매칭 전체 범위
            };

            const res = await fetch('/api/match/start', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload) // payload가 필요 없으면 이 줄 제거 가능
            });

            if (!res.ok) {
                throw new Error('HTTP ' + res.status);
            }

            const data = await res.json();
            console.log("match Start Data");
            console.log(data);
            // data 예시:
            // {status: "WAITING" }
            // {status: "MATCHED", roomId: 10, roomUuid: "....", partnerId: 2 }

            if (data.status === 'MATCHED') {
                // ✅ 채팅방 페이지 규칙에 맞게 수정
                //roomId를 어디서 받아와야함
                console.log(data);
                console.log("데이터. roomId" + data.roomId);
                sessionStorage.setItem('openRoomId', data.roomId);
                location.href = `/chat`;
                return;
            }

            // WAITING이면 폴링 시작(1초마다 매칭 결과 확인)
            startPollingForMatch(mathch_btn);

        } catch (err) {
            console.error(err);
            alert('매칭 시작 실패');
            mathch_btn.disabled = false;
        }
    });

}); //window.addEventListener('DOMContentLoaded' 끝나는 지점

function getSelectedInterests() {
    const checked = document.querySelectorAll('input[name="interestTypes"]:checked');
    if (checked.length === 0) return 'ANY';

    return Array.from(checked)
        .map(el => el.value)
        .join(',');
}

//데이터 일관성을 위해서 ALL > ANY로 바꿈
function toAny(v) {
    return (!v || v === 'ALL') ? 'ANY' : v;
}

//필터링을 form 안에 담아서 필터링키를 만듬
function buildCriteriaKey() {
    const form = document.getElementById('filterForm');
    const fd = new FormData(form);

    const g = toAny(fd.get('gender'));
    const n = toAny(fd.get('nation'));
    const lang = toAny(fd.get('studyLanguage'));

    const a1 = Number(document.getElementById('ageMin').value);
    const a2 = Number(document.getElementById('ageMax').value);
    const minAge = Math.min(a1, a2);
    const maxAge = Math.max(a1, a2);

    const levels = fd.getAll('levels');
    const lv = levels.length ? levels.join(',') : 'ANY';

    const interest = getSelectedInterests(); // 없으면 ANY

    return `g=${g}|age=${minAge}-${maxAge}|n=${n}|lang=${lang}|lv=${lv}|interest=${interest}`;
}

function clampSwap(minEl, maxEl) {
    let min = parseFloat(minEl.value);
    let max = parseFloat(maxEl.value);
    if (min > max) {
        [min, max] = [max, min];
        minEl.value = min;
        maxEl.value = max;
    }
    return { min, max };
}

function setFill(fillEl, min, max, minLimit, maxLimit) {
    const left = ((min - minLimit) / (maxLimit - minLimit)) * 100;
    const right = ((max - minLimit) / (maxLimit - minLimit)) * 100;
    fillEl.style.left = left + "%";
    fillEl.style.width = (right - left) + "%";
}

function syncAge() {
    const { min, max } = clampSwap(ageMin, ageMax);
    ageMinValue.textContent = String(min);
    ageMaxValue.textContent = String(max);
    setFill(ageFill, min, max, 18, 60);
}

// ✅ 새로고침 버튼: form reset 후 화면/리스트 갱신용 훅
document.getElementById('filterForm').addEventListener('reset', () => {
    // reset은 값이 즉시 반영되므로, 표시값도 재동기화
    setTimeout(() => {
        ageValue.textContent = ageRange.value;
        syncAge();
        // 필요하면 추천 리스트도 다시 불러오세요:
        // loadRecommend();
    }, 0);
});

// ✅ 검색 버튼: submit 이벤트 (여기서 criteriaKey 생성해서 서버로 보내도록 확장)
document.getElementById('filterForm').addEventListener('submit', (e) => {
    e.preventDefault();
    // 여기서 필터 값 읽기 가능:
    // const fd = new FormData(e.currentTarget);
    // console.log(Object.fromEntries(fd.entries()));
    // loadRecommendWithFilters(...);
});

// ✅ 자동 매칭 버튼: 기존 /api/match/start 연결
document.getElementById('auto_matching').addEventListener('click', async () => {
    // 기존 매칭 로직 붙일 자리
    // startMatching();
});

function startPollingForMatch(mathch_btn) {
    const intervalMs = 1000;
    const maxMs = 60_000; // 60초
    const startedAt = Date.now();

    const timer = setInterval(async () => {
        try {
            const res = await fetch('/api/match/result');
            if (!res.ok) {
                const body = await res.text(); // ✅ 서버가 준 에러 내용
                console.error('match/result failed:', res.status, body);
                throw new Error('HTTP ' + res.status);
            }

            const data = await res.json();

            if (data.status === 'MATCHED') {
                clearInterval(timer);
                //roomId를 어디서 받아와야함
                console.log("Polling Data 전체")
                console.log(data);
                console.log("데이터. roomId" + data.roomId);
                sessionStorage.setItem('openRoomId', data.roomId);
                location.href = `/chat`;
                return;
            }

            // ✅ 타임아웃 처리
            if (Date.now() - startedAt > maxMs) {
                clearInterval(timer);
                alert('매칭 대기 시간이 초과되었습니다. 다시 시도해주세요.');
                btn.disabled = false;
            }

        } catch (err) {
            console.error(err);
            // 폴링 중 에러가 나도 계속 시도할지/중단할지 선택 가능
            // 여기서는 3회 연속 실패 시 중단 같은 정책을 둘 수도 있습니다.
        }
    }, intervalMs);

    // (선택) 취소 버튼이 있으면 clearInterval(timer)로 중단하게 만들면 됩니다.
}

async function loadRecommend() {
    try {
        const response = await fetch('/api/recommend');

        if (!response.ok) {
            throw new Error('HTTP error ' + response.status);
        }
        const list = await response.json();
        console.log("매칭 리스트");
        console.log(list);
        renderRecommend(list);

    } catch (e) {
        console.log(e);
    }
}



function renderRecommend(list) {
    const recommendList = document.getElementById('recommendTbody');
    let html = '';

    list.forEach(item => {

        let flagStatus = "";
        let study = "";
        let languageLevel = "";
        let nation = "";
        let imagePath = item.profileImagePath + "/" + item.profileImageName;
        //console.log(item.id);

        switch (item.nativeLanguage) {
            case "KOREAN":
                flagStatus = "fi fi-kr flag-icon";
                break;
            case "JAPANESE":
                flagStatus = "fi fi-jp flag-icon";
                break;
            default:
                flagStatus = "default"
                break;
        }

        switch (item.studyLanguage) {
            case "KOREAN":
                study = "fi fi-kr";
                break;
            case "JAPANESE":
                study = "fi fi-jp";
                break;
            default:
                study = "default"
                break;
        }

        switch (item.nation) {

            case "KOREA":
                nation = "한국";
                break;
            case "JAPAN":
                nation = "일본";
                break;
            default:
                nation = "default"
                break;
        }

        switch (item.levelLanguage) {
            case 'BEGINNER':
                languageLevel = "★☆☆☆"
                break;
            case 'INTERMEDIATE':
                languageLevel = "★★☆☆"
                break;
            case 'ADVANCED':
                languageLevel = "★★★☆"
                break;
            case 'NATIVE':
                languageLevel = "★★★★"
                break;
            default:
                languageLevel = "★☆☆☆"
        }


        html += `
    <div class="member-card">
        <div class="profile-wrap">
            <img src="${imagePath}" class="profile-img" alt="">
                <span class="${flagStatus}" aria-label="Japan"></span>
        </div>

        <!-- 정보 영역 -->
        <div class="info">
            <div class="nickname">${item.nickname}</div>

            <div class="row">
                <span class="label">거주국가</span>
                <span class="value">${nation}</span>
            </div>

            <div class="row">
                <span class="label">학습언어</span>
                <span class="lang">
                    <span class="${study}"></span>
                    <span class="level">${languageLevel}</span>
                </span>
            </div>
        </div>

        <!-- 우측 영역 -->
        <div class="right">
            <span class="chatting-text">"인기 멤버예요! 🔥 답장이 늦을 수 있어요."</span>
            <div class="temp">${item.manner}℃</div>
            <button class="chat-btn" data-user-id="${item.id}">채팅 신청</button>
        </div>
    </div>
    `;
    });

    recommendList.innerHTML = html;
}

// async function chattingNumber(user_id) {
// 	try {
// 		const response = await fetch('/api/chat/partner-activity/{id}');

// 		if (!response.ok) {
// 			throw new Error('HTTP error ' + response.status);
// 		}
// 		const list = await response.json();
// 		console.log(list);
// 		renderRecommend(list);

// 	} catch (e) {
// 		console.log(e);
// 	}
// }


