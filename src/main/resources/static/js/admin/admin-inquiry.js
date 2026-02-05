let currentInquiryId = null;
let currentMode = "create"; // create | edit

// =========================
// DOM 캐시
// =========================
const modal = document.getElementById("answerModal");
const inquiryContentEl = document.getElementById("inquiryContent");
const answerTextarea = document.getElementById("answerContent");

const imageWrapper = document.getElementById("modalImageWrapper");
const imageEl = document.getElementById("modalImage");
const noImageText = document.getElementById("noImageText");

// =========================
// 모달 열기
// =========================
function openAnswerModal(inquiryId, mode) {
    currentInquiryId = inquiryId;
    currentMode = mode;

    const row = document.querySelector(`tr[data-id="${inquiryId}"]`);
    if (!row) return;

    /* 문의 내용 */
    inquiryContentEl.textContent = row.dataset.content || "";

    /* 첨부 이미지 */
    const imagePath = row.dataset.image;
    if (imagePath && imagePath.trim() !== "") {
        imageEl.src = imagePath;
        imageWrapper.classList.remove("hidden");
        noImageText.classList.add("hidden");
    } else {
        imageWrapper.classList.add("hidden");
        noImageText.classList.remove("hidden");
    }

    /* 답변 내용 */
    if (mode === "edit") {
        answerTextarea.value = row.dataset.answer || "";
    } else {
        answerTextarea.value = "";
    }

    modal.classList.remove("hidden");
}

// =========================
// 모달 닫기
// =========================
function closeAnswerModal() {
    modal.classList.add("hidden");
    answerTextarea.value = "";
    currentInquiryId = null;
    currentMode = "create";
}

// =========================
// 답변 저장 / 수정 (🔥 핵심)
// =========================
function submitAnswer() {
    const content = answerTextarea.value.trim();
    if (!content) {
        alert("답변 내용을 입력해주세요.");
        return;
    }

    const method = currentMode === "edit" ? "PUT" : "POST";

    fetch(`/admin/inquiries/${currentInquiryId}/answer`, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ content })
    })
    .then(res => {
        if (!res.ok) throw new Error();
        return res;
    })
    .then(() => {
        const row = document.querySelector(`tr[data-id="${currentInquiryId}"]`);
        if (!row) return;

        /* =========================
           1️⃣ 데이터 상태 강제 동기화
        ========================= */
        row.dataset.status = "ANSWERED";
        row.dataset.answer = content;

        /* =========================
           2️⃣ 상태 뱃지 즉시 변경
        ========================= */
        const statusSpan = row.querySelector(".status");
        statusSpan.textContent = "답변완료";
        statusSpan.className = "status answered";

        /* =========================
           3️⃣ 관리 버튼 즉시 교체
        ========================= */
        const manageTd = row.querySelector("td:last-child");
        manageTd.innerHTML = `
            <button class="btn-edit"
                onclick="openAnswerModal(${currentInquiryId}, 'edit')">
                수정
            </button>
        `;

        closeAnswerModal();
    })
    .catch(() => {
        alert("답변 처리 중 오류가 발생했습니다.");
    });
}

// =========================
// ESC 키 닫기
// =========================
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !modal.classList.contains("hidden")) {
        closeAnswerModal();
    }
});

// =========================
// 오버레이 클릭 닫기
// =========================
modal.addEventListener("click", (e) => {
    if (e.target === modal) {
        closeAnswerModal();
    }
});
