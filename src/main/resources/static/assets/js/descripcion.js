export function configurarDescripciones() {
    window.toggleDescripcion = function (link) {
        const tarjeta = link.closest('.maya-card');
        const descripcion = tarjeta.querySelector('.descripcion-corta');
        const estaExpandida = descripcion?.classList.contains('expandida');

        const todasDescripciones = document.querySelectorAll('.descripcion-corta');
        const todosLosBotones = document.querySelectorAll('.ver-mas');

        if (!descripcion) return;

        if (!estaExpandida) {
            // Cierra todas primero
            todasDescripciones.forEach(desc => desc.classList.remove('expandida'));
            todosLosBotones.forEach(btn => {
                btn.classList.remove('expandido');
                btn.textContent = 'Ver más';
            });

            // Expande la seleccionada
            descripcion.classList.add('expandida');
            link.classList.add('expandido');
            link.textContent = 'Ver menos';

        } else {
            // Contraer
            descripcion.classList.remove('expandida');
            link.classList.remove('expandido');
            link.textContent = 'Ver más';

            // Scroll suave hacia la tarjeta
            if (tarjeta) {
                setTimeout(() => {
                    tarjeta.scrollIntoView({ behavior: 'smooth', block: 'start' });

                    tarjeta.classList.add('resaltado');
                    setTimeout(() => tarjeta.classList.remove('resaltado'), 1500);
                }, 200);
            }
        }
    };
}
const sections = document.querySelectorAll('[id^="cat-"]');
const buttons = document.querySelectorAll('.category-scroll .btn');

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        buttons.forEach(btn => {
            btn.classList.toggle('active', btn.getAttribute('href').substring(1) === entry.target.id && entry.isIntersecting);
        });
    });
}, { threshold: 0.6 });

sections.forEach(section => observer.observe(section));
