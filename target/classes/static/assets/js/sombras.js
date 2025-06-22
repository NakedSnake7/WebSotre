export function configurarSombras() {
    function actualizarSombrasScroll() {
        document.querySelectorAll('.product-wrapper').forEach(wrapper => {
            const scrollArea = wrapper.querySelector('.product-scroll');
            if (!scrollArea) return;

            const tieneScroll = scrollArea.scrollWidth > scrollArea.clientWidth;
            wrapper.classList.toggle('scrollable', tieneScroll);

            const alInicio = scrollArea.scrollLeft <= 5;
            const alFinal = scrollArea.scrollLeft + scrollArea.clientWidth >= scrollArea.scrollWidth - 5;

            wrapper.classList.toggle('sombra-izquierda', !alInicio);
            wrapper.classList.toggle('sombra-derecha', !alFinal);
        });
    }

    function manejarEventoScroll() {
        document.querySelectorAll('.product-scroll').forEach(scrollArea => {
            scrollArea.addEventListener('scroll', actualizarSombrasScroll);
        });
    }

    window.addEventListener('load', () => {
        actualizarSombrasScroll();
        manejarEventoScroll();
    });

    window.addEventListener('resize', actualizarSombrasScroll);
}
