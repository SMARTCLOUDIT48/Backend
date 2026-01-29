document.addEventListener('DOMContentLoaded', () => {

    const wrapper = document.querySelector('.notice-detail-wrapper');
    const noticeId = wrapper.dataset.noticeId;

    const editBtn = document.getElementById('editBtn');
    const deleteBtn = document.getElementById('deleteBtn');

    const modal = document.getElementById('editModal');
    const titleInput = document.getElementById('editTitle');
    const contentInput = document.getElementById('editContent');

    const saveBtn = document.getElementById('saveEditBtn');
    const cancelBtn = document.getElementById('cancelEditBtn');

    /* ===== 수정 ===== */
    editBtn?.addEventListener('click', () => {
        fetch(`/customer/notice/${noticeId}/edit`)
            .then(res => res.json())
            .then(data => {
                titleInput.value = data.title;
                contentInput.value = data.content;
                modal.classList.remove('hidden');
            });
    });

    cancelBtn.addEventListener('click', () => {
        modal.classList.add('hidden');
    });

    saveBtn.addEventListener('click', () => {
        const title = titleInput.value.trim();
        const content = contentInput.value.trim();

        if (!title || !content) {
            alert('제목과 내용을 입력해주세요.');
            return;
        }

        fetch(`/customer/notice/${noticeId}/edit`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `title=${encodeURIComponent(title)}&content=${encodeURIComponent(content)}`
        })
        .then(() => {
            alert('수정되었습니다.');
            location.reload();
        });
    });

    /* ===== 삭제 ===== */
    deleteBtn?.addEventListener('click', () => {
        if (!confirm('정말 삭제하시겠습니까?')) return;

        fetch(`/customer/notice/${noticeId}/delete`, {
            method: 'POST'
        })
        .then(() => {
            alert('삭제되었습니다.');
            location.href = '/customer/notice';
        });
    });

});
