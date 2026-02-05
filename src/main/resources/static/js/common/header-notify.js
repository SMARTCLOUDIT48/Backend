/* ==========================================================
   Header Global Chat Unread Dot (🔴) - FINAL
   ✅ Any page:
     1) On load: GET /api/chat/rooms -> if hasUnread exists => show header dot
     2) Realtime: subscribe /sub/chat/notify/{myUserId} -> show header dot
   ✅ Chat page:
     - If addUnreadDotToRoom(roomId) exists, also mark sidebar list
   ========================================================== */

(function () {
    let stompClient = null;
    let notifySubscription = null;
    let connected = false;

    // ---------------------------
    // DOM helpers
    // ---------------------------
    function getMyUserId() {
        const el = document.getElementById("globalMyUserId");
        if (!el) return null;
        const v = (el.value || "").trim();
        return v ? v : null;
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

    function isChatPage() {
        // chat.html에 있는 요소 기준 (너 프로젝트 기준 roomListArea 있음)
        return document.getElementById("roomListArea") !== null;
    }

    // ---------------------------
    // 1) 서버에서 unread 여부 확인 (전역 유지 핵심)
    // ---------------------------
    function checkUnreadFromServer() {
        fetch("/api/chat/rooms")
            .then(res => (res.ok ? res.json() : []))
            .then(rooms => {
                const hasAnyUnread = Array.isArray(rooms) && rooms.some(r => r.hasUnread === true);
                if (hasAnyUnread) showHeaderUnreadDot();
                else hideHeaderUnreadDot();
            })
            .catch(err => {
                console.warn("⚠ [Header] unread check fail:", err);
                // 서버 체크 실패해도 실시간 notify는 계속 작동하니까 여기서 강제 off는 안 함
            });
    }

    // (선택) chat 페이지에서 roomList DOM 기준으로 헤더 dot 동기화하고 싶으면 사용
    // 단, chat 페이지가 아닐 때는 절대 끄지 않게 막아야 함.
    function syncHeaderDotFromRoomListDOM() {
        if (!isChatPage()) return;

        const hasAnyUnread = document.querySelector(".room-item .unread-dot") !== null;
        if (hasAnyUnread) showHeaderUnreadDot();
        else hideHeaderUnreadDot();
    }

    // ---------------------------
    // 2) STOMP 연결 + notify 구독
    // ---------------------------
    function connectAndSubscribe() {
        const myUserId = getMyUserId();
        if (!myUserId) return;

        if (typeof SockJS === "undefined" || typeof Stomp === "undefined") {
            console.warn("❌ SockJS/Stomp 라이브러리가 없습니다. (header에 CDN 추가 필요)");
            return;
        }

        // 이미 연결돼 있으면 구독만
        if (stompClient && connected) {
            subscribeNotify(myUserId);
            return;
        }

        const socket = new SockJS("/ws/chat");
        stompClient = Stomp.over(socket);
        // stompClient.debug = null; // 필요하면 주석 해제

        stompClient.connect(
            {},
            function () {
                connected = true;
                console.log("✅ [Header] STOMP connected");
                subscribeNotify(myUserId);

                // chat 페이지라면 DOM 기반 동기화 한 번
                syncHeaderDotFromRoomListDOM();
            },
            function (err) {
                connected = false;
                console.error("❌ [Header] STOMP connect fail:", err);
            }
        );
    }

    function subscribeNotify(myUserId) {
        if (!stompClient || !connected) return;
        if (notifySubscription) return; // 중복 구독 방지

        const topic = `/sub/chat/notify/${myUserId}`;

        notifySubscription = stompClient.subscribe(topic, function (message) {
            try {
                const payload = JSON.parse(message.body); // { roomId, senderId } 기대
                if (!payload || !payload.roomId) return;

                // 채팅 페이지에서 현재 보고 있는 방이면 굳이 헤더 dot 안 켜도 됨
                // (chat.js에서 처리하지만, 헤더도 안전장치)
                const currentRoomId = window.currentRoomId;
                if (currentRoomId && String(payload.roomId) === String(currentRoomId)) {
                    return;
                }

                // ✅ 헤더 dot 켜기
                showHeaderUnreadDot();

                // ✅ chat 페이지라면 방 목록에도 dot 찍기
                if (typeof window.addUnreadDotToRoom === "function") {
                    window.addUnreadDotToRoom(payload.roomId);
                }

            } catch (e) {
                console.error("❌ [Header] notify payload parse fail:", e, message.body);
            }
        });

        console.log("✅ [Header] notify subscribed:", topic);
    }

    // ---------------------------
    // init
    // ---------------------------
    document.addEventListener("DOMContentLoaded", function () {
        const myUserId = getMyUserId();

        // 비로그인: dot 숨기고 끝
        if (!myUserId) {
            hideHeaderUnreadDot();
            return;
        }

        // ✅ 1) 페이지 들어오자마자 서버로 unread 여부 확인 (전역 유지 핵심)
        checkUnreadFromServer();

        // ✅ 2) 실시간 notify 구독
        connectAndSubscribe();

        // ✅ chat 페이지에서 roomList가 fetch로 늦게 그려질 수 있으니 한번 더 동기화
        setTimeout(syncHeaderDotFromRoomListDOM, 800);
    });
})();
