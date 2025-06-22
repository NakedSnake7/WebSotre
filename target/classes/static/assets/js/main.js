import { configurarBotonWhatsApp } from './whatsapp.js';
import { configurarModal } from './modal.js';
import { configurarCarrito } from './carrito.js';
import { configurarDescripciones } from './descripcion.js';
import { configurarCategorias } from './categorias.js';
import { configurarSombras } from './sombras.js';

document.addEventListener("DOMContentLoaded", function () {
    configurarBotonWhatsApp();
    configurarModal();
    configurarCarrito();
    configurarDescripciones();
    configurarCategorias();
    configurarSombras();
});
