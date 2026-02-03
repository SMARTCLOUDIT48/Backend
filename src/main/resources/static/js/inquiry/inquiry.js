document.addEventListener('DOMContentLoaded', () => {

    const fileInput = document.getElementById('fileInput');
    const preview = document.getElementById('preview');
    const previewImg = document.getElementById('previewImg');
    const previewName = document.getElementById('previewName');
    const removeBtn = document.getElementById('removeFile');
    const currentAttachment = document.getElementById('currentAttachment');

    if (!fileInput) return;

    // =========================
    // 파일 선택
    // =========================
    fileInput.addEventListener('change', () => {
        const file = fileInput.files[0];
        if (!file) return resetPreview();

        // 기존 이미지 숨김
        if (currentAttachment) {
            currentAttachment.style.display = 'none';
        }

        // 미리보기 생성
        const reader = new FileReader();
        reader.onload = e => {
            previewImg.src = e.target.result;
        };
        reader.readAsDataURL(file);

        previewName.textContent = file.name;
        preview.classList.remove('hidden');
    });

    // =========================
    // 파일 제거
    // =========================
    if (removeBtn) {
        removeBtn.addEventListener('click', () => {
            resetPreview();
        });
    }

    // =========================
    // 초기화
    // =========================
    function resetPreview() {
        fileInput.value = '';

        previewImg.src = '';
        previewName.textContent = '';
        preview.classList.add('hidden');

        // 기존 이미지 다시 표시
        if (currentAttachment) {
            currentAttachment.style.display = 'block';
        }
    }
});
