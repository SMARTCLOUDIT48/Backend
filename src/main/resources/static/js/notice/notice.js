document.addEventListener("DOMContentLoaded", () => {
    const input = document.getElementById("noticeSearchInput");
    const listEl = document.querySelector(".notice-list");

    if (!input || !listEl) return;

    let timer = null;

    input.addEventListener("keyup", () => {
        clearTimeout(timer);

        timer = setTimeout(() => {
            const keyword = input.value.trim();
            search(keyword);
        }, 300);
    });

    function search(keyword) {
        const activeTab = document.body.dataset.activeTab;
        const type = activeTab === "faq" ? "FAQ" : "NOTICE";

        // 🔥 검색어 없으면 원래 페이지로 복귀 (가장 안전)
        if (!keyword) {
            location.reload();
            return;
        }

        fetch(`/customer/search?type=${type}&keyword=${encodeURIComponent(keyword)}`)
            .then(res => res.json())
            .then(data => renderList(data.content))
            .catch(console.error);
    }

    function renderList(list) {
        listEl.innerHTML = "";

        if (!list || list.length === 0) {
            listEl.innerHTML =
                `<li class="empty-message">검색 결과가 없습니다.</li>`;
            return;
        }

        list.forEach(item => {
            const li = document.createElement("li");

            li.innerHTML = `
                <a href="/customer/notice/${item.noticeId}"
                   class="notice-link">

                    ${item.isPinned ? `<span class="badge">공지</span>` : ""}

                    <span class="notice-title">
                        ${item.title}
                    </span>

                    <span class="notice-date">
                        ${item.createdAt.substring(0, 10)}
                    </span>

                </a>
            `;

            listEl.appendChild(li);
        });
    }
});
