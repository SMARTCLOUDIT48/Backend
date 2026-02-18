(() => {

  /* =========================
     삭제 처리
  ========================= */
  const deleteForm = document.getElementById("deleteForm");
  const boardInput = document.getElementById("delBoard");
  const idInput = document.getElementById("delId");

  document.addEventListener("click", e => {
    const deleteBtn = e.target.closest(".btn-danger[data-id]");
    if (!deleteBtn) return;

    if (!confirm("정말 삭제하시겠습니까?\n삭제 후 복구할 수 없습니다.")) return;

    boardInput.value = deleteBtn.dataset.board;
    idInput.value = deleteBtn.dataset.id;
    deleteForm.submit();
  });

  /* =========================
     모달 요소
  ========================= */
  const modal = document.getElementById("postModal");
  const modalPostId = document.getElementById("modalPostId");
  const modalBoard = document.getElementById("modalBoard");
  const modalTitle = document.getElementById("modalTitle");
  const modalContent = document.getElementById("modalContent");

  /* =========================
     상세 → 모달 열기
  ========================= */
  document.addEventListener("click", e => {
    const detailBtn = e.target.closest(".btn-detail");
    if (!detailBtn) return;

    modalPostId.value = detailBtn.dataset.id;
    modalBoard.value = detailBtn.dataset.board;

    fetch(`/admin/posts/${modalPostId.value}?board=${modalBoard.value}`)
      .then(res => {
        if (!res.ok) throw new Error();
        return res.json();
      })
      .then(data => {
        modalTitle.value = data.title;
        modalContent.value = data.content;
        modal.style.display = "flex";
      })
      .catch(() => {
        alert("게시물 정보를 불러오지 못했습니다.");
      });
  });

  /* =========================
     모달 닫기
  ========================= */
  modal.addEventListener("click", e => {
    if (e.target === modal) modal.style.display = "none";
  });

  document.querySelector(".btn-cancel").addEventListener("click", () => {
    modal.style.display = "none";
  });

  /* =========================
     수정 저장 (⭐ 핵심 수정)
  ========================= */
  document.querySelector(".btn-save").addEventListener("click", () => {
    if (!confirm("수정 내용을 저장하시겠습니까?")) return;

    fetch("/admin/posts/update", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      },
      body: new URLSearchParams({
        id: modalPostId.value,
        board: modalBoard.value,
        title: modalTitle.value,
        content: modalContent.value
      })
    })
      .then(res => {
        if (!res.ok) throw new Error();
        alert("수정이 완료되었습니다.");
        location.reload();
      })
      .catch(() => {
        alert("수정에 실패했습니다.");
      });
  });

})();
