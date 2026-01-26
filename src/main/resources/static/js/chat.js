/* ==========================================================
   LangMate Global Chat Logic (Final Integrated Version)
   ========================================================== */

// --- 1. 전역 설정 ---
const myNativeLanguage = 'KO'; // 나의 모국어 (KO: 한국어)
var stompClient = null;
var currentRoomId = null;
var mySenderId = Math.floor(Math.random() * 1000) + 1; // 내 ID (임시 랜덤)
var mySenderName = "익명" + mySenderId;
var subscription = null;
var aiData = {};

// --- 2. 페이지 로드 시 실행 ---
document.addEventListener('DOMContentLoaded', () => {
    console.log("Chat Init...");
    loadChatRooms();
    createLoadingOverlay(); // 로딩 오버레이 DOM 생성
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

// --- 3. 채팅방 목록 불러오기 ---
function loadChatRooms() {
    fetch('/api/chat/rooms')
        .then(res => res.json())
        .then(rooms => {
            const listArea = document.getElementById("roomListArea");
            listArea.innerHTML = "";
            rooms.forEach(room => {
                const li = document.createElement("li");
                li.className = "room-item";
                li.onclick = () => enterRoom(room.id, room.name, li);
                li.innerHTML = `
                    <div class="room-avatar">💬</div>
                    <div class="room-info">
                        <div class="room-name">${room.name}</div>
                        <div class="room-last-msg">ID: ${room.id}</div>
                    </div>`;
                listArea.appendChild(li);
            });
        })
        .catch(err => console.error("방 목록 로딩 실패:", err));
}

// --- 4. 방 입장 ---
function enterRoom(roomId, roomName, element) {
    if (currentRoomId === roomId) return;

    currentRoomId = roomId;

    // 헤더 정보 업데이트
    document.getElementById("roomTitle").innerText = roomName;

    // 배지 초기화 (일단 숨김)
    const badge = document.getElementById('activityBadge');
    if (badge) badge.style.display = 'none';

    document.getElementById("messageList").innerHTML = "";

    document.querySelectorAll(".room-item").forEach(item => item.classList.remove("active"));
    if(element) element.classList.add("active");

    connect(roomId);
}

// --- 5. 소켓 연결 (수정됨) ---
function connect(roomId) {
    if (stompClient && stompClient.connected) {
        subscribeToRoom(roomId);
        return;
    }

    // 1. 로그인할 때 저장해둔 토큰 꺼내기 (키 이름이 'accessToken'인지 확인하세요!)
    var token = localStorage.getItem("accessToken");

    if (!token) {
        alert("로그인이 필요합니다!");
        window.location.href = "/login"; // 로그인 페이지로 튕겨내기
        return;
    }

    // 2. SockJS 사용 + URL 뒤에 토큰 붙이기 (?token=eyJ...)
    // 백엔드에서 .withSockJS()를 켰으므로 new SockJS()를 써야 합니다.
    var socket = new SockJS('/ws/chat?token=' + token);

    stompClient = Stomp.over(socket);

    // 3. 연결 시 헤더에도 토큰 담기 (이중 보안)
    var headers = {
        'Authorization': 'Bearer ' + token
    };

    stompClient.connect(headers, function (frame) {
        console.log('Connected: ' + frame);
        document.getElementById("connectionStatus").innerText = "🟢 실시간 연결됨";
        document.getElementById("connectionStatus").style.color = "green";
        subscribeToRoom(roomId);
    }, function(error) {
        // 연결 실패 시 에러 처리
        console.error("연결 실패:", error);
        alert("서버 연결에 실패했습니다. 토큰이 만료되었거나 서버 오류입니다.");
    });
}

// --- 6. 방 구독 (핵심 로직 수정됨) ---
function subscribeToRoom(roomId) {
    if (subscription) subscription.unsubscribe();

    subscription = stompClient.subscribe('/sub/chat/room/' + roomId, function (message) {
        const msgObj = JSON.parse(message.body);

        // 1. UI 그리기
        showUi(msgObj);

        // 2. ✨ [추가] 상대방이 메시지를 보냈다면 활동량 배지 즉시 갱신
        if (msgObj.senderId != mySenderId) {
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
                    // 상대방 ID 찾기 (마지막 메시지 기준)
                    if(msg.senderId != mySenderId) {
                        lastPartnerId = msg.senderId;
                    }
                });
                showSystemMessage("--- 이전 대화 내역 ---");
            }

            // ✨ [추가] 과거 대화 내역을 불러온 후, 상대방의 활동량 체크 실행
            if (lastPartnerId) {
                checkPartnerActivity(lastPartnerId);
            }
        });
}

// ==========================================================
// ✨ 8. UI 그리기 (프로필 사진 + 카톡 스타일 레이아웃)
// ==========================================================
function showUi(message) {
    var ul = document.getElementById("messageList");
    var li = document.createElement("li");

    var isMe = (message.senderId == mySenderId);
    li.className = isMe ? "message-li me right" : "message-li other left";

    // --- 1. 프로필 이미지 (상대방일 때만) ---
    if (!isMe) {
        const profileImg = document.createElement("img");
        profileImg.src = getProfileImage(message.senderId, message.sender);
        profileImg.className = "profile-img";
        li.appendChild(profileImg);
    }

    // --- 2. 메인 컨테이너 (이름 + 내용래퍼) ---
    // 이름은 말풍선 위에, 말풍선과 시간은 옆에 와야 하므로 별도 컨테이너가 필요
    const mainContainer = document.createElement("div");
    mainContainer.style.display = "flex";
    mainContainer.style.flexDirection = "column";
    mainContainer.style.maxWidth = "70%";

    // (1) 이름 표시 (상대방일 때만 메인 컨테이너 맨 위에)
    if (!isMe) {
        const senderDiv = document.createElement("div");
        senderDiv.className = "sender-name";
        senderDiv.innerText = message.sender;
        mainContainer.appendChild(senderDiv);
    }

    // (2) 내용 래퍼 (말풍선 + 시간 + 읽음숫자) -> 여기가 CSS flex-row 적용됨
    const contentWrapper = document.createElement("div");
    contentWrapper.className = "msg-content-wrapper";

    // --- A. 말풍선 영역 (툴바 포함) ---
    const bubbleArea = document.createElement("div");
    bubbleArea.style.position = "relative"; // 툴바 위치 기준

    // 말풍선 내용 처리
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
        ttsBtn.innerHTML = '<i class="fa-solid fa-volume-high"></i> 🔊'; // 아이콘 텍스트 대체 가능
        ttsBtn.onclick = () => speakText(cleanText);
        actionToolbar.appendChild(ttsBtn);
    }

    // 번역 버튼
    const transResultBox = document.createElement("div");
    transResultBox.className = "trans-box";
    transResultBox.innerText = "번역 중...";

    if (message.type === 'TALK' || !message.type) {
        const transBtn = document.createElement("button");
        transBtn.className = "action-btn";
        transBtn.innerHTML = "🇰🇷↔🇯🇵";
        transBtn.onclick = function() {
            if (transResultBox.style.display === "block") {
                transResultBox.style.display = "none";
            } else {
                transResultBox.style.display = "block";
                requestTranslation(message.message, transResultBox);
            }
        };
        actionToolbar.appendChild(transBtn);
    }

    // 말풍선 DOM 조립
    const bubbleDiv = document.createElement("div");
    bubbleDiv.className = "bubble";
    bubbleDiv.innerHTML = bubbleContent;

    bubbleArea.appendChild(actionToolbar);
    bubbleArea.appendChild(bubbleDiv);
    bubbleArea.appendChild(transResultBox);

    // --- B. 메타 정보 (읽음 숫자 + 시간) ---
    const metaDiv = document.createElement("div");
    metaDiv.className = "msg-meta";

    // 1. 읽음 숫자 (데이터가 없으면 0 처리)
    const unReadCount = message.unReadCount || 0;
    if (unReadCount > 0) {
        const readSpan = document.createElement("span");
        readSpan.className = "read-status";
        readSpan.innerText = unReadCount;
        metaDiv.appendChild(readSpan);
    }

    // 2. 시간 (데이터가 없으면 현재 시간 포맷팅)
    const timeSpan = document.createElement("span");
    timeSpan.className = "send-time";
    // message.time이 서버에서 오면 그대로 쓰고, 없으면 JS에서 만듦
    timeSpan.innerText = message.time ? message.time : formatTime(new Date());
    metaDiv.appendChild(timeSpan);

    // --- 최종 조립 ---
    contentWrapper.appendChild(bubbleArea);
    contentWrapper.appendChild(metaDiv); // 말풍선 옆에 메타정보 붙이기

    mainContainer.appendChild(contentWrapper);
    li.appendChild(mainContainer);
    ul.appendChild(li);
    ul.scrollTop = ul.scrollHeight;
}

// 8 - 2
// 현재 시간을 '오후 3:04' 형식으로 반환하는 함수
function formatTime(date) {
    const d = new Date(date);
    let hour = d.getHours();
    let min = d.getMinutes();
    const ampm = hour >= 12 ? '오후' : '오전';

    hour = hour % 12;
    hour = hour ? hour : 12; // 0시는 12시로 표시
    min = min < 10 ? '0' + min : min;

    return `${ampm} ${hour}:${min}`;
}

// --- 9. 시스템 메시지 ---
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
        .catch(err => {
            resultBox.innerText = "❌ 번역 실패";
        });
}

// --- 12. AI 문법 검사 (모달) ---
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

// --- 14. TTS (말하기) ---
function speakText(text, lang) {
    if (!window.speechSynthesis) { alert("TTS 미지원 브라우저"); return; }
    window.speechSynthesis.cancel();
    var utterance = new SpeechSynthesisUtterance(text);
    const isKorean = /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/.test(text);
    utterance.lang = lang ? lang : (isKorean ? 'ko-KR' : 'ja-JP');
    window.speechSynthesis.speak(utterance);
}


/* ==========================================================
   💘 1. 전체 호감도 분석
   ========================================================== */
function checkLoveSignal() {
    if (!currentRoomId) { alert("대화방에 먼저 입장해주세요!"); return; }

    const messages = document.querySelectorAll(".message-li .bubble");
    if (messages.length === 0) { alert("분석할 대화 내용이 없어요!"); return; }

    let chatLog = "";
    const recentMessages = Array.from(messages).slice(-15);
    recentMessages.forEach(msg => {
        let text = msg.innerText.replace("🔊", "").replace("🔄", "").replace("번역", "").trim();
        chatLog += text + "\n";
    });

    const btn = document.querySelector(".love-btn-header");
    const btnSpan = btn.querySelector("span");
    const originalText = btnSpan.innerText;

    btnSpan.innerText = "분석중...";
    btn.disabled = true;

    const overlay = document.getElementById("loadingOverlay");
    if(overlay) overlay.style.display = "flex";

    fetch('/api/ai/sentiment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ chatHistory: chatLog })
    })
        .then(res => res.json())
        .then(data => {
            showLoveModal(data);
        })
        .catch(err => {
            console.error(err);
            alert("분석 실패!");
        })
        .finally(() => {
            btnSpan.innerText = originalText;
            btn.disabled = false;
            if(overlay) overlay.style.display = "none";
        });
}

function showLoveModal(data) {
    const modal = document.getElementById("loveModal");
    const title = document.getElementById("loveModalTitle");
    const scoreDiv = document.getElementById("loveScore");
    const feedbackDiv = document.getElementById("loveFeedback");

    document.getElementById("loveRiskBadge").style.display = "none";
    document.getElementById("loveRecommendationBox").style.display = "none";

    title.innerText = "💘 호감도 전체 분석";

    let emoji = "😐";
    if (data.score >= 90) emoji = "😍";
    else if (data.score >= 70) emoji = "😘";
    else if (data.score <= 30) emoji = "😱";

    scoreDiv.innerHTML = `${data.score}점 <span style="font-size:2rem">${emoji}</span>`;
    feedbackDiv.innerHTML = `<b>[평가]</b> ${data.comment}<br><br><b>[💡 조언]</b> ${data.advice}`;

    modal.style.display = "block";
}

function closeLoveModal() {
    document.getElementById("loveModal").style.display = "none";
}


/* ==========================================================
   💌 2. 보내기 전 멘트 체크
   ========================================================== */
function checkMessageScore() {
    var msgInput = document.getElementById("msg");
    var content = msgInput.value.trim();

    if (!content) {
        alert("내용을 입력해주세요!");
        msgInput.focus();
        return;
    }

    var btn = document.getElementById("btn-love-check");
    var originalHTML = btn.innerHTML;
    btn.innerText = "⏳";
    btn.disabled = true;

    fetch('/api/ai/pre-check', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ "message": content })
    })
        .then(res => res.json())
        .then(data => {
            showLoveTooltip(data);
        })
        .catch(err => {
            console.error(err);
            alert("오류 발생!");
        })
        .finally(() => {
            btn.innerHTML = originalHTML;
            btn.disabled = false;
        });
}

function showLoveTooltip(data) {
    const tooltip = document.getElementById("loveTooltip");
    const scoreSpan = document.getElementById("tooltipScore");
    const feedbackDiv = document.getElementById("tooltipFeedback");
    const recommendBox = document.getElementById("tooltipRecommendBox");

    let emoji = "😐";
    if (data.score >= 90) emoji = "😍";
    else if (data.score >= 70) emoji = "😘";
    else if (data.score <= 30) emoji = "😱";

    scoreSpan.innerHTML = `${data.score}점 ${emoji} <span style="font-size:0.8rem; color:#666;">(${data.risk})</span>`;
    feedbackDiv.innerText = data.feedback;

    if (data.better_version && data.better_version.trim() !== "") {
        recommendBox.style.display = "block";
        recommendBox.innerHTML = `
            <span class="recommend-label">✨ 추천 멘트 (클릭하여 적용)</span>
            <div class="recommend-text">"${data.better_version}"</div>
            <span class="click-hint">Click!</span>
        `;
        recommendBox.dataset.text = data.better_version;
    } else {
        recommendBox.style.display = "none";
    }

    tooltip.style.display = "block";
}

function applyTooltipCorrection() {
    const recommendBox = document.getElementById("tooltipRecommendBox");
    const newText = recommendBox.dataset.text;
    const msgInput = document.getElementById("msg");

    if (newText) {
        msgInput.value = newText;
        closeLoveTooltip();
        msgInput.focus();
    }
}

function closeLoveTooltip() {
    document.getElementById("loveTooltip").style.display = "none";
}


/* ==========================================================
   🖼️ 3. 유틸리티 (프로필 이미지 & 활동량 체크)
   ========================================================== */

// 사용자 ID와 이름을 받아서, 보여줄 프로필 이미지 URL을 반환
function getProfileImage(userId, userName) {
    // 임시 아바타 생성 (나중에 실제 DB 연동 시 변경)
    return `https://ui-avatars.com/api/?name=${encodeURIComponent(userName)}&background=random&color=fff&rounded=true`;
}

// ✨ 상대방의 활동량(인기도) 체크 및 배지 표시
function checkPartnerActivity(partnerId) {
    if (!partnerId) return;

    fetch(`/chat/activity/${partnerId}`)
        .then(res => res.json())
        .then(count => {
            const badge = document.getElementById('activityBadge');
            if (!badge) return; // HTML에 배지가 없으면 패스

            badge.style.display = 'inline-block';
            badge.className = 'activity-badge'; // 클래스 초기화

            if (count >= 10) {
                badge.classList.add('badge-hot');
                badge.innerHTML = `🔥 ${count}명과 대화 중! (인기)`;
            } else if (count > 0) {
                badge.classList.add('badge-normal');
                badge.innerHTML = `💬 오늘 ${count}명과 대화함`;
            } else {
                badge.classList.add('badge-normal');
                badge.innerHTML = `✨ 지금 대화하면 칼답 가능성!`;
            }
        })
        .catch(err => console.error("활동량 조회 실패:", err));
}