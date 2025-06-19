export function configurarCategorias() {
    const tabContainer = document.getElementById('pills-tab');
    if (tabContainer) {
        const tabs = Array.from(tabContainer.querySelectorAll('.nav-link'));
        let startX = 0;
        document.addEventListener('touchstart', e => startX = e.touches[0].clientX);
        document.addEventListener('touchend', e => {
            const deltaX = e.changedTouches[0].clientX - startX;
            if (Math.abs(deltaX) < 50) return;
            const activeIndex = tabs.findIndex(tab => tab.classList.contains('active'));
            if (deltaX < 0 && activeIndex < tabs.length - 1) tabs[activeIndex + 1].click();
            else if (deltaX > 0 && activeIndex > 0) tabs[activeIndex - 1].click();
        });
    }

    const sections = document.querySelectorAll("[id^='cat-']");
    const links = document.querySelectorAll(".category-scroll a");

    function onScrollCategoria() {
        let current = "";
        sections.forEach(section => {
            if (section.getBoundingClientRect().top <= 100) {
                current = section.id;
            }
        });

        links.forEach(link => {
            link.classList.toggle("active", link.getAttribute("href") === `#${current}`);
        });
    }

    window.addEventListener("scroll", onScrollCategoria);
    onScrollCategoria();
}
