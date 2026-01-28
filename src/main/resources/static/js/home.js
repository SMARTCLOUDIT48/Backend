document.addEventListener('DOMContentLoaded', () => {
    const track = document.querySelector('.slider-track');
    const cards = Array.from(document.querySelectorAll('.community-card'));
    const dots = document.querySelectorAll('.dot');

    const prevBtn = document.querySelector('.slider-btn.prev');
    const nextBtn = document.querySelector('.slider-btn.next');

    const cardWidth = 340;
    const gap = 32;

    let current = 1; // 가운데 카드

    function updateSlider(animate = true) {
        const moveX = -(cardWidth + gap) * current;

        track.style.transition = animate
            ? 'transform 0.7s cubic-bezier(0.22, 1, 0.36, 1)'
            : 'none';

        track.style.transform = `translateX(${moveX}px)`;

        cards.forEach((card, i) => {
            const diff = Math.abs(i - current);

            if (diff === 0) {
                card.style.transform = 'scale(1)';
                card.style.opacity = '1';
                card.style.zIndex = '3';
            } else if (diff === 1) {
                card.style.transform = 'scale(0.9)';
                card.style.opacity = '0.6';
                card.style.zIndex = '2';
            } else {
                card.style.transform = 'scale(0.82)';
                card.style.opacity = '0.3';
                card.style.zIndex = '1';
            }
        });

        dots.forEach((dot, i) => {
            dot.classList.toggle('active', i === current);
        });
    }

    nextBtn.addEventListener('click', () => {
        if (current < cards.length - 1) {
            current++;
            updateSlider();
        }
    });

    prevBtn.addEventListener('click', () => {
        if (current > 0) {
            current--;
            updateSlider();
        }
    });

    dots.forEach((dot, i) => {
        dot.addEventListener('click', () => {
            current = i;
            updateSlider();
        });
    });

    updateSlider(false);
});
