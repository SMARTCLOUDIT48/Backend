/* ==========================================================
   LangMate Global Chat Logic (Final Integrated Version)
   ========================================================== */

// --- 1. 전역 설정 ---
const myNativeLanguage = 'KO'; // 나의 모국어 (KO: 한국어)
var stompClient = null;
var currentRoomId = null;
var notifySubscription = null;

// 내 정보 가져오기
var mySenderId = document.getElementById("myUserId").value;
var mySenderName = document.getElementById("myNickname").value;

var subscription = null;
var aiData = {};

// --- 2. 페이지 로드 시 실행 ---
document.addEventListener('DOMContentLoaded', () => {
    console.log(`✅ 채팅 초기화 완료 (내 ID: ${mySenderId}, 닉네임: ${mySenderName})`);
    loadChatRooms();
    createLoadingOverlay();
});

// 로딩 오버레이 동적 생성
function createLoadingOverlay() {
    if (!document.getElementById("loadingOverlay")) {
        const overlay = document.createElement("div");
        overlay.id = "loadingOverlay";
        overlay.style.cssText = `
            display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(255, 255, 255, 0.8); z-index: 9999;
            align-items: center; justify-content: center; flex-direction: column;
            backdrop-filter: blur(5px);
        `;
        overlay.innerHTML = `
            <div style="font-size: 4rem; animation: heartBeat 1s infinite;">💖</div>
            <div style="margin-top: 20px; font-size: 1.5rem; font-weight: bold; color: #ff4081;">
                AI가 두 분의 기류를 분석 중입니다...
            </div>
        `;
        document.body.appendChild(overlay);
    }
}

// --- 3. 채팅방 목록 로드 ---
function loadChatRooms() {
    fetch('/api/chat/rooms')
        .then(res => res.json())
        .then(rooms => {
            console.log("📌 서버에서 온 방 데이터:", rooms);

            const listArea = document.getElementById("roomListArea");
            listArea.innerHTML = "";

            // ✅ 헤더 dot 초기화 후, unread 있으면 켜기
            hideHeaderUnreadDot();
            const anyUnread = rooms.some(r => r.hasUnread === true);
            if (anyUnread) showHeaderUnreadDot();

            rooms.forEach(room => {
                const roomId = room.roomId;
                const roomName = room.roomName;

                const li = document.createElement("li");
                li.className = "room-item";
                li.dataset.roomId = String(roomId);
                li.onclick = () => enterRoom(roomId, roomName, li);

                const unreadDot = room.hasUnread ? `<span class="unread-dot"></span>` : ``;

                li.innerHTML = `
                    <div class="room-avatar">💬</div>
                    <div class="room-info">
                        <div class="room-name">
                            ${roomName}
                            ${unreadDot}
                        </div>
                        <div class="room-last-msg">ID: ${roomId}</div>
                    </div>
                `;

                listArea.appendChild(li);
            });
        })
        .catch(err => console.error("방 목록 로딩 실패:", err));
}



// --- 4. 방 입장 (🔴 읽음 처리 추가) ---
function enterRoom(roomId, roomName, element) {
    if (currentRoomId === roomId) return;

    currentRoomId = roomId;
    document.getElementById("roomTitle").innerText = roomName;

    // ✅ 읽음 처리 API 호출
    fetch(`/api/chat/rooms/${roomId}/read`, { method: "POST" })
        .catch(err => console.error("읽음 처리 API 호출 실패:", err));

    // ✅ UI에서 해당 방 🔴 제거
    removeUnreadDotFromRoom(roomId);

    // ✅ 헤더 🔴는 "다른 방에 unread가 남아있는지" 보고 결정
    const stillUnreadExists = document.querySelector(".room-item .unread-dot") !== null;
    if (!stillUnreadExists) hideHeaderUnreadDot();

    // 활동 배지 숨김
    const badge = document.getElementById('activityBadge');
    if (badge) badge.style.display = 'none';

    // 메시지 영역 초기화
    document.getElementById("messageList").innerHTML = "";

    // 활성화 스타일 변경
    document.querySelectorAll(".room-item").forEach(item => item.classList.remove("active"));
    if (element) element.classList.add("active");

    // 소켓 연결
    connect(roomId);

    // 오른쪽 사이드바
    loadPartnerInfo(roomId).catch(err => {
        console.error("API 호출 에러:", err);

        const sidebar = document.getElementById("partnerProfileArea");
        if (sidebar) sidebar.style.display = "flex";

        document.getElementById("partnerName").innerText = "(알 수 없음)";
        document.getElementById("partnerIntro").innerText = "정보를 불러올 수 없습니다.";
    });
}





// --- 5. 소켓 연결 ---
function connect(roomId) {
    if (stompClient && stompClient.connected) {
        subscribeToRoom(roomId);

        // ✅ [NEW] 실시간 🔴 알림 구독 (한 번만)
        subscribeToNotifications();

        return;
    }

    var socket = new SockJS('/ws/chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        document.getElementById("connectionStatus").innerText = "🟢 실시간 연결됨";
        document.getElementById("connectionStatus").style.color = "green";

        subscribeToRoom(roomId);

        // ✅ [NEW] 실시간 🔴 알림 구독 (한 번만)
        subscribeToNotifications();

    }, function (error) {
        console.error("연결 실패:", error);
    });
}


// --- 6. 방 구독 ---
function subscribeToRoom(roomId) {
    if (subscription) subscription.unsubscribe();

    subscription = stompClient.subscribe('/sub/chat/room/' + roomId, function (message) {
        const msgObj = JSON.parse(message.body);
        showUi(msgObj);

        // 내가 아닌 경우 활동량 체크
        if (String(msgObj.senderId) !== String(mySenderId)) {
            checkPartnerActivity(msgObj.senderId);
        }
    });

    loadChatHistory(roomId);
}

// --- 7. 이전 대화 내역 ---
function loadChatHistory(roomId) {
    fetch('/chat/history/' + roomId)
        .then(res => res.json())
        .then(messages => {
            const ul = document.getElementById("messageList");
            ul.innerHTML = "";
            let lastPartnerId = null;

            if (messages && messages.length > 0) {
                messages.forEach(msg => {
                    showUi(msg);
                    if (String(msg.senderId) !== String(mySenderId)) {
                        lastPartnerId = msg.senderId;
                    }
                });
                showSystemMessage("--- 이전 대화 내역 ---");
            }

            if (lastPartnerId) {
                checkPartnerActivity(lastPartnerId);
            }
        });
}

// ==========================================================
// ✨ 8. UI 그리기
// ==========================================================
function showUi(message) {
    var ul = document.getElementById("messageList");
    var li = document.createElement("li");

    var isMe = (String(message.senderId) === String(mySenderId));
    li.className = isMe ? "message-li me right" : "message-li other left";

    // 프로필 이미지 (상대방만)
    if (!isMe) {
        const profileImg = document.createElement("img");
        profileImg.src = getProfileImage(message.senderId, message.sender);
        profileImg.className = "profile-img";
        li.appendChild(profileImg);
    }

    // 메인 컨테이너
    const mainContainer = document.createElement("div");
    mainContainer.style.display = "flex";
    mainContainer.style.flexDirection = "column";
    mainContainer.style.maxWidth = "70%";

    // 이름 표시 (상대방만)
    if (!isMe) {
        const senderDiv = document.createElement("div");
        senderDiv.className = "sender-name";
        senderDiv.innerText = message.sender;
        mainContainer.appendChild(senderDiv);
    }

    const contentWrapper = document.createElement("div");
    contentWrapper.className = "msg-content-wrapper";

    // 말풍선 처리
    const bubbleArea = document.createElement("div");
    bubbleArea.style.position = "relative";

    let bubbleContent = "";
    let cleanText = "";

    if (message.type === 'VOICE') {
        bubbleContent = `<audio controls src="${message.message}" style="height:30px; width:220px;"></audio>`;
        cleanText = "음성 메시지입니다.";
    } else {
        bubbleContent = message.message;
        var tempDiv = document.createElement("div");
        tempDiv.innerHTML = message.message;
        cleanText = tempDiv.innerText.replace("🎤", "").replace("[음성 메시지]", "").trim();
    }

    // 툴바 (TTS, 번역)
    const actionToolbar = document.createElement("div");
    actionToolbar.className = "msg-actions";

    if (cleanText.length > 0) {
        const ttsBtn = document.createElement("button");
        ttsBtn.className = "action-btn";
        ttsBtn.innerHTML = '<i class="fa-solid fa-volume-high"></i> 🔊';
        ttsBtn.onclick = () => speakText(cleanText);
        actionToolbar.appendChild(ttsBtn);
    }

    const transResultBox = document.createElement("div");
    transResultBox.className = "trans-box";
    transResultBox.innerText = "번역 중...";

    if (message.type === 'TALK' || !message.type) {
        const transBtn = document.createElement("button");
        transBtn.className = "action-btn";
        transBtn.innerHTML = "🇰🇷↔🇯🇵";
        transBtn.onclick = function () {
            if (transResultBox.style.display === "block") {
                transResultBox.style.display = "none";
            } else {
                transResultBox.style.display = "block";
                requestTranslation(message.message, transResultBox);
            }
        };
        actionToolbar.appendChild(transBtn);
    }

    const bubbleDiv = document.createElement("div");
    bubbleDiv.className = "bubble";
    bubbleDiv.innerHTML = bubbleContent;

    bubbleArea.appendChild(actionToolbar);
    bubbleArea.appendChild(bubbleDiv);
    bubbleArea.appendChild(transResultBox);

    // 시간 및 읽음 카운트
    const metaDiv = document.createElement("div");
    metaDiv.className = "msg-meta";

    const unReadCount = message.unReadCount || 0;
    if (unReadCount > 0) {
        const readSpan = document.createElement("span");
        readSpan.className = "read-status";
        readSpan.innerText = unReadCount;
        metaDiv.appendChild(readSpan);
    }

    const timeSpan = document.createElement("span");
    timeSpan.className = "send-time";
    timeSpan.innerText = message.time ? message.time : formatTime(new Date());
    metaDiv.appendChild(timeSpan);

    contentWrapper.appendChild(bubbleArea);
    contentWrapper.appendChild(metaDiv);

    mainContainer.appendChild(contentWrapper);
    li.appendChild(mainContainer);
    ul.appendChild(li);
    ul.scrollTop = ul.scrollHeight;
}

// --- 유틸리티 함수 ---
function formatTime(date) {
    const d = new Date(date);
    let hour = d.getHours();
    let min = d.getMinutes();
    const ampm = hour >= 12 ? '오후' : '오전';
    hour = hour % 12;
    hour = hour ? hour : 12;
    min = min < 10 ? '0' + min : min;
    return `${ampm} ${hour}:${min}`;
}

function showSystemMessage(text) {
    var ul = document.getElementById("messageList");
    var li = document.createElement("li");
    li.className = "message-li center";
    li.innerHTML = `<div class="bubble">${text}</div>`;
    ul.appendChild(li);
    ul.scrollTop = ul.scrollHeight;
}

// --- 10. 메시지 전송 ---
function sendMessage() {
    closeLoveTooltip();

    if (!currentRoomId) { alert("방을 선택해주세요!"); return; }
    if (currentVoiceBlob) { uploadAndSendVoice(); return; }

    var msgInput = document.getElementById("msg");
    var content = msgInput.value.trim();

    if (content && stompClient) {
        var chatMessage = {
            type: 'TALK',
            roomId: currentRoomId,
            sender: mySenderName,
            senderId: mySenderId,
            message: content
        };
        stompClient.send("/pub/chat/message", {}, JSON.stringify(chatMessage));
        msgInput.value = '';
    }
}

// --- 11. 번역 요청 ---
function requestTranslation(text, resultBox) {
    resultBox.style.display = 'block';
    if (resultBox.dataset.translated === "true") return;

    fetch('/api/ai/translate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: text, targetLang: myNativeLanguage })
    })
        .then(res => res.json())
        .then(data => {
            resultBox.innerText = "✅ " + data.translated;
            resultBox.dataset.translated = "true";
        })
        .catch(err => { resultBox.innerText = "❌ 번역 실패"; });
}

// --- 12. AI 문법 검사 ---
function checkGrammar() {
    var msgInput = document.getElementById("msg");
    var text = msgInput.value.trim();
    if (!text) { alert("내용을 입력해주세요!"); return; }

    document.getElementById("aiModal").style.display = 'block';
    document.getElementById("aiCorrectedText").innerText = "Thinking... 🧠";
    document.getElementById("aiExplanationText").innerText = "";

    fetch('/api/ai/grammar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ "message": text })
    })
        .then(res => res.json())
        .then(data => {
            aiData = data;
            document.getElementById("aiCorrectedText").innerText = data.corrected;
            switchTab('kr');
        });
}
function switchTab(lang) {
    if (!aiData.corrected) return;
    document.getElementById("tabKr").className = (lang === 'kr') ? "ai-tab active" : "ai-tab";
    document.getElementById("tabJp").className = (lang === 'jp') ? "ai-tab active" : "ai-tab";
    const text = (lang === 'kr') ? aiData.explanation_kr : aiData.explanation_jp;
    document.getElementById("aiExplanationText").innerText = text || "설명 없음";
}
function closeAiModal() { document.getElementById("aiModal").style.display = 'none'; }
function applyCorrection() {
    if (aiData.corrected) {
        document.getElementById("msg").value = aiData.corrected;
        closeAiModal();
    }
}

// --- 13. 음성 녹음 및 전송 ---
var mediaRecorder = null;
var audioChunks = [];
var currentVoiceBlob = null;
var isRecording = false;

function toggleRecording() {
    if (!isRecording) {
        navigator.mediaDevices.getUserMedia({ audio: true }).then(stream => {
            mediaRecorder = new MediaRecorder(stream);
            audioChunks = [];
            mediaRecorder.ondataavailable = e => audioChunks.push(e.data);
            mediaRecorder.onstop = () => {
                currentVoiceBlob = new Blob(audioChunks, { type: 'audio/webm' });
                document.getElementById("preview-player").src = URL.createObjectURL(currentVoiceBlob);
                document.getElementById("preview-box").style.display = "flex";
            };
            mediaRecorder.start();
            isRecording = true;
            document.getElementById("btn-mic").classList.add("recording");
        });
    } else {
        mediaRecorder.stop();
        isRecording = false;
        document.getElementById("btn-mic").classList.remove("recording");
    }
}
function cancelVoice() {
    currentVoiceBlob = null;
    document.getElementById("preview-box").style.display = "none";
}
function uploadAndSendVoice() {
    var msgInput = document.getElementById("msg");
    msgInput.placeholder = "AI가 듣고 변환 중입니다... 🎧";
    msgInput.disabled = true;

    var formData = new FormData();
    formData.append("file", currentVoiceBlob, "voice.webm");

    fetch("/api/ai/voice-send", { method: "POST", body: formData })
        .then(r => r.json())
        .then(data => {
            var combinedMessage = `[음성 메시지] 🎤<br>${data.text}<br><br><audio controls src="${data.audioUrl}" style="height:30px; width:200px;"></audio>`;
            var chatMessage = {
                type: 'TALK',
                roomId: currentRoomId,
                sender: mySenderName,
                senderId: mySenderId,
                message: combinedMessage
            };
            stompClient.send("/pub/chat/message", {}, JSON.stringify(chatMessage));
            cancelVoice();
            msgInput.placeholder = "메시지를 입력하세요...";
            msgInput.disabled = false;
        })
        .catch(err => {
            console.error(err);
            alert("음성 변환 실패!");
            msgInput.disabled = false;
        });
}

// --- 14. TTS ---
function speakText(text, lang) {
    if (!window.speechSynthesis) { alert("TTS 미지원 브라우저"); return; }
    window.speechSynthesis.cancel();
    var utterance = new SpeechSynthesisUtterance(text);
    const isKorean = /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/.test(text);
    utterance.lang = lang ? lang : (isKorean ? 'ko-KR' : 'ja-JP');
    window.speechSynthesis.speak(utterance);
}

// --- 15. 호감도 체크 ---
function checkLoveSignal() {
    if (!currentRoomId) { alert("대화방에 먼저 입장해주세요!"); return; }
    const messages = document.querySelectorAll(".message-li .bubble");
    if (messages.length === 0) { alert("분석할 대화 내용이 없어요!"); return; }

    let chatLog = "";
    Array.from(messages).slice(-15).forEach(msg => {
        let text = msg.innerText.replace("🔊", "").replace("🔄", "").replace("번역", "").trim();
        chatLog += text + "\n";
    });

    const btn = document.querySelector(".love-btn-header");
    const btnSpan = btn.querySelector("span");
    const originalText = btnSpan.innerText;
    btnSpan.innerText = "분석중...";
    btn.disabled = true;
    const overlay = document.getElementById("loadingOverlay");
    if (overlay) overlay.style.display = "flex";

    fetch('/api/ai/sentiment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ chatHistory: chatLog })
    }).then(res => res.json()).then(data => showLoveModal(data))
        .catch(err => { console.error(err); alert("분석 실패!"); })
        .finally(() => { btnSpan.innerText = originalText; btn.disabled = false; if (overlay) overlay.style.display = "none"; });
}
function showLoveModal(data) {
    const modal = document.getElementById("loveModal");
    const scoreDiv = document.getElementById("loveScore");
    const feedbackDiv = document.getElementById("loveFeedback");
    document.getElementById("loveRiskBadge").style.display = "none";
    document.getElementById("loveRecommendationBox").style.display = "none";

    let emoji = "😐";
    if (data.score >= 90) emoji = "😍"; else if (data.score >= 70) emoji = "😘"; else if (data.score <= 30) emoji = "😱";
    scoreDiv.innerHTML = `${data.score}점 <span style="font-size:2rem">${emoji}</span>`;
    feedbackDiv.innerHTML = `<b>[평가]</b> ${data.comment}<br><br><b>[💡 조언]</b> ${data.advice}`;
    modal.style.display = "block";
}
function closeLoveModal() { document.getElementById("loveModal").style.display = "none"; }

function checkMessageScore() {
    var msgInput = document.getElementById("msg");
    var content = msgInput.value.trim();
    if (!content) { alert("내용을 입력해주세요!"); msgInput.focus(); return; }
    var btn = document.getElementById("btn-love-check");
    var originalHTML = btn.innerHTML;
    btn.innerText = "⏳"; btn.disabled = true;
    fetch('/api/ai/pre-check', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ "message": content })
    }).then(res => res.json()).then(data => showLoveTooltip(data))
        .catch(err => { console.error(err); alert("오류 발생!"); })
        .finally(() => { btn.innerHTML = originalHTML; btn.disabled = false; });
}
function showLoveTooltip(data) {
    const tooltip = document.getElementById("loveTooltip");
    const scoreSpan = document.getElementById("tooltipScore");
    const feedbackDiv = document.getElementById("tooltipFeedback");
    const recommendBox = document.getElementById("tooltipRecommendBox");
    let emoji = "😐";
    if (data.score >= 90) emoji = "😍"; else if (data.score >= 70) emoji = "😘"; else if (data.score <= 30) emoji = "😱";
    scoreSpan.innerHTML = `${data.score}점 ${emoji} <span style="font-size:0.8rem; color:#666;">(${data.risk})</span>`;
    feedbackDiv.innerText = data.feedback;
    if (data.better_version && data.better_version.trim() !== "") {
        recommendBox.style.display = "block";
        recommendBox.innerHTML = `<span class="recommend-label">✨ 추천 멘트</span><div class="recommend-text">"${data.better_version}"</div>`;
        recommendBox.dataset.text = data.better_version;
    } else { recommendBox.style.display = "none"; }
    tooltip.style.display = "block";
}
function applyTooltipCorrection() {
    const recommendBox = document.getElementById("tooltipRecommendBox");
    const newText = recommendBox.dataset.text;
    const msgInput = document.getElementById("msg");
    if (newText) { msgInput.value = newText; closeLoveTooltip(); msgInput.focus(); }
}
function closeLoveTooltip() { document.getElementById("loveTooltip").style.display = "none"; }
function getProfileImage(userId, userName) { return `https://ui-avatars.com/api/?name=${encodeURIComponent(userName)}&background=random&color=fff&rounded=true`; }
function checkPartnerActivity(partnerId) {
    if (!partnerId) return;
    fetch(`/chat/activity/${partnerId}`).then(res => res.json()).then(count => {
        const badge = document.getElementById('activityBadge');
        if (!badge) return;
        badge.style.display = 'inline-block';
        badge.className = 'activity-badge';
        if (count >= 10) { badge.classList.add('badge-hot'); badge.innerHTML = `🔥 ${count}명과 대화 중! (인기)`; }
        else if (count > 0) { badge.classList.add('badge-normal'); badge.innerHTML = `💬 오늘 ${count}명과 대화함`; }
        else { badge.classList.add('badge-normal'); badge.innerHTML = `✨ 지금 대화하면 칼답 가능성!`; }
    }).catch(err => console.error("활동량 조회 실패:", err));
}


// ==========================================================
// ✅ [NEW] 16. 상대방 프로필 정보 로드 (사이드바용) - 수정됨
// ==========================================================
function loadPartnerInfo(roomId) {
    const sidebar = document.getElementById("partnerProfileArea");
    if (!sidebar) return;

    // 1. 초기화 (로딩 중 표시)
    // 기존 데이터가 잠깐 보이는 것을 방지하기 위해 초기화합니다.
    document.getElementById("partnerName").innerText = "Loading...";
    document.getElementById("partnerIntro").innerText = "...";
    document.getElementById("partnerImg").src = "/images/profile/default.png";
    document.getElementById("partnerNationText").innerText = "";
    document.getElementById("partnerAge").innerText = "";

    // 2. 실제 API 호출
    fetch(`/api/chat/room/${roomId}`)
        .then(res => {
            if (!res.ok) throw new Error("프로필 정보 로드 실패");
            return res.json();
        })
        .then(data => {
            console.log("📌 상대방 정보:", data);
            updatePartnerProfileUI(data);
        })
        .catch(err => {
            console.error("API 호출 에러:", err);
            // 에러 발생 시 '알 수 없음' 처리
            document.getElementById("partnerName").innerText = "(알 수 없음)";
            document.getElementById("partnerIntro").innerText = "상대방 정보를 불러올 수 없습니다.";
        });
}

// ==========================================================
// UI 업데이트 함수 (최신 DTO 반영 완료)
// ==========================================================
function updatePartnerProfileUI(data) {
    const sidebar = document.getElementById("partnerProfileArea");
    if (sidebar) sidebar.style.display = "flex";

    // 1. 닉네임
    document.getElementById("partnerName").innerText = data.opponentNickname || "알 수 없음";

    // 2. 프로필 이미지
    const imgPath = data.opponentProfileImg ? data.opponentProfileImg : "/images/profile/default.png";
    const imgTag = document.getElementById("partnerImg");
    if (imgTag) imgTag.src = imgPath;

    // 3. 국적 (DB에서 가져온 값 표시)
    document.getElementById("partnerNationText").innerText = data.opponentNation || "Unknown";
    document.getElementById("partnerNationFlag").innerText = "🏳️"; // 국기는 일단 고정 (추후 매핑 가능)

    // 4. 자기소개
    document.getElementById("partnerIntro").innerText = data.opponentIntro || "자기소개가 없습니다.";

    // 5. [NEW] 나이 표시 (백엔드에서 가져옴!)
    const ageElem = document.getElementById("partnerAge");
    if (ageElem) {
        if (data.opponentAge && data.opponentAge > 0) {
            ageElem.innerText = data.opponentAge + "세";
        } else {
            ageElem.innerText = ""; // 나이 정보 없으면 공란
        }
    }

    // 6. [NEW] '상대방 프로필 확인' 버튼 링크 걸기
    const profileBtn = document.getElementById("opponentProfileBtn");
    if (profileBtn) {
        if (data.opponentId && data.opponentId !== 0) {
            // 예: /member/profile/3 (상대방 ID로 이동)
            profileBtn.href = "/member/profile/" + data.opponentId;
            profileBtn.style.display = "inline-block";
            profileBtn.innerText = "상대방 프로필 확인 >";
        } else {
            // 상대방 정보가 없으면 버튼 숨김
            profileBtn.href = "#";
            profileBtn.style.display = "none";
        }
    }
}
function addUnreadDotToRoom(roomId) {
    const roomItem = document.querySelector(`.room-item[data-room-id="${String(roomId)}"]`);
    if (!roomItem) return;

    // 이미 있으면 중복 생성 X
    if (roomItem.querySelector(".unread-dot")) return;

    const nameDiv = roomItem.querySelector(".room-name");
    if (!nameDiv) return;

    const dot = document.createElement("span");
    dot.className = "unread-dot";
    nameDiv.appendChild(dot);
}

function removeUnreadDotFromRoom(roomId) {
    const roomItem = document.querySelector(`.room-item[data-room-id="${String(roomId)}"]`);
    if (!roomItem) return;

    const dot = roomItem.querySelector(".unread-dot");
    if (dot) dot.remove();
}

function subscribeToNotifications() {
    // 이미 구독했으면 중복 방지
    if (notifySubscription) return;

    const topic = `/sub/chat/notify/${mySenderId}`;

    notifySubscription = stompClient.subscribe(topic, function (message) {
        try {
            const payload = JSON.parse(message.body); // { roomId: 1, senderId: 2 }
            if (!payload || !payload.roomId) return;

            // 내가 현재 보고 있는 방이면 🔴 필요 없음
            if (String(payload.roomId) === String(currentRoomId)) return;

            // ✅ 방 목록 🔴
            addUnreadDotToRoom(payload.roomId);

            // ✅ 헤더 🔴
            showHeaderUnreadDot();

        } catch (e) {
            console.error("notify payload parse 실패:", e, message.body);
        }
    });

    console.log("✅ notify 구독 완료:", topic);
}

function showHeaderUnreadDot() {
    const dot = document.getElementById("headerUnreadDot");
    if (!dot) return;
    dot.style.display = "inline-block";
}
function hideHeaderUnreadDot() {
    const dot = document.getElementById("headerUnreadDot");
    if (!dot) return;
    dot.style.display = "none";
}

