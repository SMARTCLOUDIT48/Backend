(() => {
  "use strict";

  /* =========================
     1. 상태 관리
  ========================= */
  let currentType = "users";   // users | posts | inquiries
  let currentRange = "daily";  // daily | weekly | monthly
  let chart = null;

  /* =========================
     2. Chart 초기화
  ========================= */
  const canvas = document.getElementById("adminChart");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  // 그라디에이션 (배경 톤과 어울리게)
  const gradient = ctx.createLinearGradient(0, 0, 0, 280);
  gradient.addColorStop(0, "rgba(99, 179, 237, 0.45)");
  gradient.addColorStop(1, "rgba(99, 179, 237, 0.05)");

  chart = new Chart(ctx, {
    type: "line",
    data: {
      labels: [],
      datasets: [{
        label: "",
        data: [],
        fill: true,
        backgroundColor: gradient,
        borderColor: "#63b3ed",
        borderWidth: 2,
        tension: 0.4,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: "#63b3ed",
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: "rgba(30, 41, 59, 0.9)",
          titleColor: "#fff",
          bodyColor: "#fff",
          padding: 10,
          cornerRadius: 8,
        }
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: { color: "#6b7280", font: { size: 12 } }
        },
        y: {
          beginAtZero: true,
          grid: { color: "rgba(0,0,0,0.04)" },
          ticks: { color: "#6b7280", font: { size: 12 } }
        }
      }
    }
  });

  /* =========================
     3. 데이터 로드 (실제 API)
     ※ 백엔드에서 아래 형태 JSON 반환 필요
     {
       labels: [...],
       values: [...]
     }
  ========================= */
  async function loadChartData() {
    try {
      const res = await fetch(
        `/admin/dashboard/stats?type=${currentType}&range=${currentRange}`
      );

      if (!res.ok) throw new Error("통계 데이터 로드 실패");

     const data = await res.json();

     chart.data.labels = data.labels ?? [];
     chart.data.datasets[0].data = data.values ?? [];
     chart.data.datasets[0].label = getChartLabel();
     chart.update();


      updateDescription();

    } catch (error) {
      console.error(error);
    }
  }

  /* =========================
     4. 라벨 / 설명 텍스트
  ========================= */
  function getChartLabel() {
    if (currentType === "users") return "회원 수 추이";
    if (currentType === "posts") return "게시글 수 추이";
    return "문의 수 추이";
  }

  function updateDescription() {
    const desc = document.querySelector(".stats-hint");
    if (!desc) return;

    let typeText =
      currentType === "users" ? "회원" :
      currentType === "posts" ? "게시글" : "문의";

    let rangeText =
      currentRange === "daily" ? "일별" :
      currentRange === "weekly" ? "주별" : "월별";

    desc.textContent = `※ ${rangeText} ${typeText} 기준 통계입니다.`;
  }

  /* =========================
     5. 데이터 종류 탭 (회원/게시글/문의)
  ========================= */
  const dataTabs = document.querySelectorAll(".data-tab");
  dataTabs.forEach(tab => {
    tab.addEventListener("click", () => {
      const type = tab.dataset.type;
      if (type === currentType) return;

      dataTabs.forEach(t => t.classList.remove("active"));
      tab.classList.add("active");

      currentType = type;
      loadChartData();
    });
  });

  /* =========================
     6. 기간 필터 탭 (일/주/월)
  ========================= */
  const rangeTabs = document.querySelectorAll(".filter-btn");
  rangeTabs.forEach(btn => {
    btn.addEventListener("click", () => {
      const range = btn.dataset.range;
      if (range === currentRange) return;

      rangeTabs.forEach(b => {
        b.classList.remove("active");
        b.setAttribute("aria-selected", "false");
      });

      btn.classList.add("active");
      btn.setAttribute("aria-selected", "true");

      currentRange = range;
      loadChartData();
    });
  });

  /* =========================
     7. 최초 로드
  ========================= */
  loadChartData();

})();
