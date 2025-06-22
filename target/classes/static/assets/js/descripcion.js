export function configurarDescripciones() {
    window.toggleDescripcion = function (link) {
        const tarjeta = link.closest('.card');
        const descripcion = tarjeta.querySelector('.descripcion-corta');
        const estaExpandida = descripcion?.classList.contains('expandida');

        const todasDescripciones = document.querySelectorAll('.descripcion-corta');
        const todosLosBotones = document.querySelectorAll('.ver-mas');

        if (!descripcion) return;

        if (!estaExpandida) {
            todasDescripciones.forEach(desc => desc.classList.remove('expandida'));
            todosLosBotones.forEach(btn => {
                btn.classList.remove('expandido');
                btn.querySelector("span").textContent = 'Ver más';
            });

            descripcion.classList.add('expandida');
            link.classList.add('expandido');
            link.querySelector("span").textContent = 'Ver menos';

        } else {
            descripcion.classList.remove('expandida');
            link.classList.remove('expandido');
            link.querySelector("span").textContent = 'Ver más';

            if (tarjeta) {
                setTimeout(() => {
                    tarjeta.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    tarjeta.classList.add('resaltado');
                    setTimeout(() => tarjeta.classList.remove('resaltado'), 2000);
                }, 200);
            }
        }
    };
}

