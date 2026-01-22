/* ==========================================================
   LangMate Global Chat Logic
   ========================================================== */

// --- 전역 설정 ---
const myNativeLanguage = 'KO'; // 번역 타겟 언어 (KO: 한국어)
var stompClient = null;
var currentRoomId = null;
var mySenderId = 1;     // 임시 ID
var mySenderName = "팀장님"; // 임시 이름
var subscription = null;
var aiData = {};

// --- 페이지 로드 시 실행 ---
document.addEventListener('DOMContentLoaded', () => {
    loadChatRooms();
});

// --- 채팅방 목록 불러오기 ---
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
        });
}

// --- 방 입장 ---
function enterRoom(roomId, roomName, element) {
    if (currentRoomId === roomId) return;
    currentRoomId = roomId;
    document.getElementById("roomTitle").innerText = roomName;
    document.getElementById("messageList").innerHTML = "";
    document.querySelectorAll(".room-item").forEach(item => item.classList.remove("active"));
    if(element) element.classList.add("active");
    connect(roomId);
}

// --- 소켓 연결 ---
function connect(roomId) {
    if (stompClient && stompClient.connected) {
        subscribeToRoom(roomId);
        return;
    }
    var socket = new WebSocket('ws://localhost:8080/ws/chat');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        document.getElementById("connectionStatus").innerText = "🟢 실시간 연결됨";
        document.getElementById("connectionStatus").style.color = "green";
        subscribeToRoom(roomId);
    });
}

function subscribeToRoom(roomId) {
    if (subscription) subscription.unsubscribe();
    subscription = stompClient.subscribe('/sub/chat/room/' + roomId, function (message) {
        showUi(JSON.parse(message.body));
    });
    loadChatHistory(roomId);
}

// --- 대화 내역 불러오기 ---
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
// ✨ UI 그리기 (번역 기능 포함)
// ==========================================================
function showUi(message) {
    var ul = document.getElementById("messageList");
    var li = document.createElement("li");

    var isMe = (message.senderId == mySenderId);
    li.className = isMe ? "message-li me" : "message-li other";

    let bubbleContent = "";
    if (message.type === 'VOICE') {
        bubbleContent = `<audio controls src="${message.message}" style="height:30px; width:220px;"></audio>`;
    } else {
        bubbleContent = message.message;
    }

    li.innerHTML = `
        <div class="sender">${message.sender}</div>
        <div class="bubble">
            <span class="msg-content">${bubbleContent}</span>
        </div>`;

    ul.appendChild(li);

    // 🟢 [수정됨] 내 메시지든 아니든 번역 버튼이 뜨도록 '!isMe' 제거함
    if (message.type === 'TALK') {
        const bubble = li.querySelector('.bubble');

        const transBox = document.createElement('div');
        transBox.className = 'trans-result';
        transBox.innerText = '번역 중...';

        const transBtn = document.createElement('button');
        transBtn.className = 'trans-btn';
        transBtn.innerText = '🔄 번역';
        transBtn.onclick = function() {
            if (transBox.style.display === 'block') {
                transBox.style.display = 'none';
            } else {
                requestTranslation(message.message, transBox);
            }
        };

        bubble.appendChild(transBtn);
        bubble.appendChild(transBox);
    }

    ul.scrollTop = ul.scrollHeight;
}

// --- 기타 유틸리티 함수들 ---
function showSystemMessage(text) {
    var ul = document.getElementById("messageList");
    var li = document.createElement("li");
    li.className = "message-li center";
    li.innerHTML = `<div class="bubble">${text}</div>`;
    ul.appendChild(li);
    ul.scrollTop = ul.scrollHeight;
}

function sendMessage() {
    if (!currentRoomId) { alert("방을 선택해주세요!"); return; }
    if (currentVoiceBlob) { uploadAndSendVoice(); return; }

    var msgInput = document.getElementById("msg");
    if (msgInput.value && stompClient) {
        stompClient.send("/pub/chat/message", {}, JSON.stringify({
            type: 'TALK',
            roomId: currentRoomId,
            sender: mySenderName,
            senderId: mySenderId,
            message: msgInput.value
        }));
        msgInput.value = '';
    }
}

function requestTranslation(text, resultBox) {
    resultBox.style.display = 'block';
    if (resultBox.dataset.translated === "true") return;

    fetch('/api/ai/translate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            message: text,
            targetLang: myNativeLanguage
        })
    })
        .then(res => res.json())
        .then(data => {
            resultBox.innerText = "✅ " + data.translated;
            resultBox.dataset.translated = "true";
        })
        .catch(err => {
            console.error(err);
            resultBox.innerText = "❌ 번역 실패";
        });
}

// --- AI 문법 ---
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

// --- 음성 녹음 ---
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
    var formData = new FormData();
    formData.append("file", currentVoiceBlob, "voice.webm");
    fetch("/api/file/upload", { method: "POST", body: formData })
        .then(r => r.json())
        .then(data => {
            stompClient.send("/pub/chat/message", {}, JSON.stringify({
                type: 'VOICE', roomId: currentRoomId, sender: mySenderName, senderId: mySenderId, message: data.url
            }));
            cancelVoice();
        });
}