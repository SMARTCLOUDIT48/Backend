let currentInquiryId = null;
let currentMode = "create"; // create | edit

// =========================
// 모달 열기 (답변 / 수정)
// =========================
function openAnswerModal(inquiryId, mode) {
    currentInquiryId = inquiryId;
    currentMode = mode;

    const row = document.querySelector(`tr[data-id="${inquiryId}"]`);
    if (!row) return;

    // 문의 내용
    document.getElementById("inquiryContent").textContent =
        row.dataset.content || "";

    // 첨부 이미지
    const imagePath = row.dataset.image;
    const imageWrapper = document.getElementById("modalImageWrapper");
    const image = document.getElementById("modalImage");
    const noImageText = document.getElementById("noImageText");

    if (imagePath && imagePath.trim() !== "") {
        image.src = imagePath;
        imageWrapper.classList.remove("hidden");
        noImageText.classList.add("hidden");
    } else {
        imageWrapper.classList.add("hidden");
        noImageText.classList.remove("hidden");
    }

    // 답변 내용 (수정 시 기존 답변)
    const answerTextarea = document.getElementById("answerContent");
    if (mode === "edit") {
        answerTextarea.value = row.dataset.answer || "";
    } else {
        answerTextarea.value = "";
    }

    document.getElementById("answerModal").classList.remove("hidden");
}

// =========================
// 모달 닫기
// =========================
function closeAnswerModal() {
    document.getElementById("answerModal").classList.add("hidden");
    currentInquiryId = null;
    currentMode = "create";
}

// =========================
// 답변 저장 / 수정
// =========================
function submitAnswer() {
    const content = document.getElementById("answerContent").value.trim();

    if (!content) {
        alert("답변 내용을 입력해주세요.");
        return;
    }

    const method = currentMode === "edit" ? "PUT" : "POST";

    fetch(`/admin/inquiries/${currentInquiryId}/answer`, {
        method,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ content })
    })
        .then(res => {
            if (!res.ok) throw new Error();
            closeAnswerModal();

            const row = document.querySelector(
                `tr[data-id="${currentInquiryId}"]`
            );
            if (!row) return;

            // 상태 변경
            const statusSpan = row.querySelector(".status");
            statusSpan.textContent = "답변완료";
            statusSpan.classList.remove("waiting");
            statusSpan.classList.add("answered");

            // 답변 내용 저장 (다음 수정 대비)
            row.dataset.answer = content;

            // 관리 버튼 교체
            const manageTd = row.lastElementChild;
            manageTd.innerHTML = `
                <button class="btn-edit"
                    onclick="openAnswerModal(${currentInquiryId}, 'edit')">
                    수정
                </button>
            `;
        })
        .catch(() => {
            alert("답변 처리 중 오류가 발생했습니다.");
        });
}

// =========================
// ESC 키로 모달 닫기
// =========================
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
        closeAnswerModal();
    }
});

// =========================
// 오버레이 클릭 시 닫기
// =========================
document.getElementById("answerModal")?.addEventListener("click", (e) => {
    if (e.target.id === "answerModal") {
        closeAnswerModal();
    }
});
