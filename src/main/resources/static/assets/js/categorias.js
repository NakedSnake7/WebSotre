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
    const scrollBar = document.querySelector(".category-scroll");

    const indicator = document.querySelector(".category-indicator");

    /* ============================
       FUNCIÓN QUE MUEVE EL INDICADOR
       ============================ */
	   function moverIndicador(btn) {
	       if (!indicator || !btn) return;

	       const rect = btn.getBoundingClientRect();
	       const containerRect = scrollBar.getBoundingClientRect();

	       const newLeft = rect.left - containerRect.left + scrollBar.scrollLeft;

	       const oldTransform = indicator.style.transform;
	       const oldLeft = oldTransform.includes("translateX(")
	           ? parseFloat(oldTransform.split("(")[1])
	           : newLeft;

	       // Configurar variables CSS para la animación
	       indicator.style.setProperty("--from", `${oldLeft}px`);
	       indicator.style.setProperty("--to", `${newLeft}px`);

	       // Aplicar ancho
	       indicator.style.width = `${rect.width}px`;

	       // Activar animación bounce
	       indicator.style.animation = "bounceSlide 0.35s cubic-bezier(.25,1.5,.5,1)";

	       // Al terminar, fijar posición final en transform
	       indicator.addEventListener("animationend", () => {
	           indicator.style.transform = `translateX(${newLeft}px)`;
	           indicator.style.animation = "none";
	       }, { once: true });
	   }


    /* === AUTO-CENTRAR CATEGORÍA AL HACER CLICK === */
    links.forEach(btn => {
        btn.addEventListener("click", () => {
            const rect = btn.getBoundingClientRect();
            const containerRect = scrollBar.getBoundingClientRect();
            const offset = rect.left - containerRect.left - (containerRect.width / 2) + (rect.width / 2);

            scrollBar.scrollBy({
                left: offset,
                behavior: "smooth"
            });

            moverIndicador(btn);
        });
    });

    /* === CAMBIAR ACTIVE SEGÚN EL SCROLL === */
    function onScrollCategoria() {
        let current = "";

        sections.forEach(section => {
            if (section.getBoundingClientRect().top <= 100) {
                current = section.id;
            }
        });

        links.forEach(link => {
            const isActive = link.getAttribute("href") === `#${current}`;
            link.classList.toggle("active", isActive);

            if (isActive) {
                // Mover el indicador
                moverIndicador(link);

                // Auto-centrar
                const rect = link.getBoundingClientRect();
                const containerRect = scrollBar.getBoundingClientRect();
                const offset = rect.left - containerRect.left - (containerRect.width / 2) + (rect.width / 2);

                scrollBar.scrollBy({
                    left: offset,
                    behavior: "smooth"
                });
            }
        });
    }

    /* === INICIALIZAR INDICADOR AL CARGAR === */
    const inicial = document.querySelector(".category-scroll .btn.active");
    if (inicial) moverIndicador(inicial);

    window.addEventListener("scroll", onScrollCategoria);
    onScrollCategoria();
}
