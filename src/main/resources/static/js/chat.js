/* ==========================================================
   LangMate Global Chat Logic (Updated for New UI)
   ========================================================== */

// --- 1. 전역 설정 ---
const myNativeLanguage = 'KO'; // 나의 모국어 (KO: 한국어)
var stompClient = null;
var currentRoomId = null;
var mySenderId = Math.floor(Math.random() * 1000) + 1;
var mySenderName = "익명" + mySenderId;
var subscription = null;
var aiData = {};

// --- 2. 페이지 로드 시 실행 ---
document.addEventListener('DOMContentLoaded', () => {
    console.log("Chat Init...");
    loadChatRooms();
    createLoadingOverlay(); // 로딩 오버레이 DOM 생성 (없을 경우 대비)
});

// 로딩 오버레이 동적 생성 (HTML에 누락되었을 경우를 위한 안전장치)
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
    document.getElementById("roomTitle").innerText = roomName;
    document.getElementById("messageList").innerHTML = "";

    document.querySelectorAll(".room-item").forEach(item => item.classList.remove("active"));
    if(element) element.classList.add("active");

    connect(roomId);
}

// --- 5. 소켓 연결 ---
function connect(roomId) {
    if (stompClient && stompClient.connected) {
        subscribeToRoom(roomId);
        return;
    }

    var socket = new WebSocket('ws://localhost:8080/ws/chat');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        document.getElementById("connectionStatus").innerText = "🟢 실시간 연결됨";
        document.getElementById("connectionStatus").style.color = "green";
        subscribeToRoom(roomId);
    });
}

// --- 6. 방 구독 ---
function subscribeToRoom(roomId) {
    if (subscription) subscription.unsubscribe();

    subscription = stompClient.subscribe('/sub/chat/room/' + roomId, function (message) {
        showUi(JSON.parse(message.body));
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
            if (messages && messages.length > 0) {
                messages.forEach(msg => showUi(msg));
                showSystemMessage("--- 이전 대화 내역 ---");
            }
        });
}

// ==========================================================
// ✨ 8. UI 그리기 (ToolBar 스타일 적용)
// ==========================================================
function showUi(message) {
    var ul = document.getElementById("messageList");
    var li = document.createElement("li");

    var isMe = (message.senderId == mySenderId);
    li.className = isMe ? "message-li me" : "message-li other";

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

    const senderDiv = document.createElement("div");
    senderDiv.className = "sender";
    senderDiv.innerText = message.sender;

    const bubbleDiv = document.createElement("div");
    bubbleDiv.className = "bubble";

    const contentDiv = document.createElement("div");
    contentDiv.className = "msg-content";
    contentDiv.innerHTML = bubbleContent;
    bubbleDiv.appendChild(contentDiv);

    const actionToolbar = document.createElement("div");
    actionToolbar.className = "msg-actions";

    if (cleanText.length > 0) {
        const ttsBtn = document.createElement("button");
        ttsBtn.className = "action-btn";
        ttsBtn.innerHTML = "🔊";
        ttsBtn.title = "듣기 (TTS)";
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
        transBtn.title = "번역 보기";

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

    bubbleDiv.appendChild(actionToolbar);
    li.appendChild(senderDiv);
    li.appendChild(bubbleDiv);
    li.appendChild(transResultBox);

    ul.appendChild(li);
    ul.scrollTop = ul.scrollHeight;
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
   💘 1. 전체 호감도 분석 (Updated for New Header UI)
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

    // ✨ UI 업데이트: 헤더 버튼 클래스 변경 (.love-btn-header)
    const btn = document.querySelector(".love-btn-header");
    const btnSpan = btn.querySelector("span"); // 텍스트가 들어있는 span 선택
    const originalText = btnSpan.innerText;

    // 로딩 상태 시작
    btnSpan.innerText = "분석중...";
    btn.disabled = true;

    // 오버레이 표시
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
            // 로딩 상태 종료
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
   💌 2. 보내기 전 멘트 체크 (Updated for Tooltip UI)
   ========================================================== */
function checkMessageScore() {
    var msgInput = document.getElementById("msg");
    var content = msgInput.value.trim();

    if (!content) {
        alert("내용을 입력해주세요!");
        msgInput.focus();
        return;
    }

    // 로딩 표시 (입력창 왼쪽 작은 버튼)
    var btn = document.getElementById("btn-love-check");
    var originalHTML = btn.innerHTML; // 아이콘 유지를 위해 HTML 저장
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
            btn.innerHTML = originalHTML; // 원래 아이콘 복구
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

    // 추천 멘트가 있을 때만 표시
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
        msgInput.focus(); // 입력창으로 포커스 이동
    }
}

function closeLoveTooltip() {
    document.getElementById("loveTooltip").style.display = "none";
}