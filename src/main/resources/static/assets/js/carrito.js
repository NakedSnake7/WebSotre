export function configurarCarrito() {
const cartButton = document.getElementById('cartButton');
const cartDropdown = document.getElementById('cartDropdown');
const cartItems = document.getElementById('cartItems');
const cartTotal = document.getElementById('cartTotal');
const modalTotal = document.getElementById('modalTotal');
const checkoutButton = document.getElementById('checkoutButton');
const checkoutModal = document.getElementById('checkoutModal');
const finalizeButton = document.getElementById("finalizeButton");
const checkoutForm = document.getElementById('checkoutForm');
const LIMITE_ENVIO_GRATIS = 1250;
const COSTO_ENVIO = 120;

let selectedProducts = [];
let isProcessing = false;

function saveCart(subtotal = 0, envio = 0, totalFinal = 0) {
    localStorage.setItem('cartData', JSON.stringify({
        products: selectedProducts,
        subtotal,
        envio,
        total: totalFinal
    }));
}

function loadCart() {
    const savedCart = localStorage.getItem('cartData');
    if (savedCart) {
        const cartData = JSON.parse(savedCart);
        selectedProducts = cartData.products || [];
    }
    updateCart();
}

function updateCart() {
    if (!cartItems) return;
    cartItems.innerHTML = '';

    let subtotal = 0;
    selectedProducts.forEach(p => subtotal += p.price * p.quantity);
    const envio = subtotal >= LIMITE_ENVIO_GRATIS ? 0 : COSTO_ENVIO;
    const totalFinal = subtotal + envio;

    selectedProducts.forEach(p => {
        const li = document.createElement('li');
        li.className = 'd-flex justify-content-between align-items-center mb-2';
        li.innerHTML = `
            <div><b>${p.name}</b> - $${p.price} x ${p.quantity}</div>
            <div class="d-flex align-items-center">
                <input type="number" class="remove-quantity" min="1" max="${p.quantity}" value="1" data-product="${p.name}" style="width: 3rem; text-align: center; margin-right: 5px;">
                <button class="btn btn-danger btn-sm remove-button" data-product="${p.name}">Eliminar</button>
            </div>
        `;
        cartItems.appendChild(li);
    });

    if (cartTotal) cartTotal.textContent = `$${totalFinal.toFixed(2)}`;
    if (modalTotal) modalTotal.textContent = `$${totalFinal.toFixed(2)}`;

    // Barra de envío gratis
    const envioMensaje = document.getElementById('envioGratisMensaje');
    const envioBarra = document.getElementById('envioGratisBarra');
    const envioContainer = document.getElementById('envioGratisContainer');
    if (envioContainer && envioMensaje && envioBarra) {
        envioContainer.style.display = selectedProducts.length ? 'block' : 'none';
        if (subtotal >= LIMITE_ENVIO_GRATIS) {
            envioMensaje.textContent = "🎉 ¡Tienes envío gratis!";
            envioBarra.style.width = "100%";
        } else {
            const faltante = LIMITE_ENVIO_GRATIS - subtotal;
            const progreso = (subtotal / LIMITE_ENVIO_GRATIS) * 100;
            envioMensaje.textContent = `Agrega $${faltante.toFixed(2)} más para envío gratis`;
            envioBarra.style.width = `${progreso.toFixed(0)}%`;
        }
    }

    if (cartDropdown) cartDropdown.style.display = selectedProducts.length ? 'block' : 'none';

    const resumen = document.getElementById('cartResumenDesglose');
    if (resumen) resumen.innerHTML = `<div><b>Subtotal:</b> $${subtotal.toFixed(2)}</div><div><b>Envío:</b> $${envio.toFixed(2)}</div>`;
    const modalResumen = document.getElementById('modalResumenDesglose');
    if (modalResumen) modalResumen.innerHTML = `<div><b>Subtotal:</b> $${subtotal.toFixed(2)}</div><div><b>Envío:</b> $${envio.toFixed(2)}</div>`;

    saveCart(subtotal, envio, totalFinal);
}

function addToCart(name, price, quantityId, stock) {
    const input = document.getElementById(quantityId);
    const qty = parseInt(input?.value) || 0;
    if (qty <= 0) return alert('Cantidad inválida');
    const existing = selectedProducts.find(p => p.name === name);
    if (existing) {
        if (existing.quantity + qty > stock) {
            alert(`Solo hay ${stock} unidades disponibles`);
            return;
        }
        existing.quantity += qty;
    } else {
        if (qty > stock) {
            alert(`Solo hay ${stock} unidades disponibles`);
            return;
        }
        selectedProducts.push({ name, price, quantity: qty });
    }

    // Animación de carrito
    cartButton.classList.add('cart-animate');
    setTimeout(() => cartButton.classList.remove('cart-animate'), 500);

    updateCart();
}

function removeFromCart(name, qty) {
    const p = selectedProducts.find(p => p.name === name);
    if (p) {
        p.quantity -= qty;
        if (p.quantity <= 0) selectedProducts = selectedProducts.filter(pr => pr.name !== name);
    }
    updateCart();
}

// Delegación de eventos para botones "Agregar al carrito"
document.body.addEventListener('click', function(e){
    const btn = e.target.closest('.add-to-cart');
    if (!btn) return;
    const name = btn.dataset.name;
    const price = parseFloat(btn.dataset.price);
    const quantityId = btn.dataset.quantityId;
    const stock = parseInt(btn.dataset.stock) || 999;
    addToCart(name, price, quantityId, stock);
});

// Remover productos del carrito
if (cartItems) {
    cartItems.addEventListener('click', e => {
        if (e.target.classList.contains('remove-button')) {
            const name = e.target.dataset.product;
            const qty = parseInt(e.target.previousElementSibling.value);
            if (qty > 0) removeFromCart(name, qty);
        }
    });
}

// Mostrar/Ocultar carrito
if (cartButton && cartDropdown) cartButton.addEventListener('click', () => {
    cartDropdown.style.display = cartDropdown.style.display === 'block' ? 'none' : 'block';
});
if (checkoutButton && checkoutModal) checkoutButton.addEventListener('click', () => checkoutModal.style.display = 'block');

// Finalizar compra
if (finalizeButton && checkoutForm) {
    finalizeButton.addEventListener('click', async () => {
        if (isProcessing) return;
        isProcessing = true;
        finalizeButton.disabled = true;
        finalizeButton.textContent = "Procesando...";

        if (!validateForm(checkoutForm)) {
            alert('Corrige los errores en el formulario');
            resetFinalize();
            return;
        }

        const fullName = document.getElementById('fullName').value.trim();
        const email = document.getElementById('CosEmail').value.trim();
        const phone = document.getElementById('phone').value.trim();
        const address = document.getElementById('address').value.trim();
        const subtotal = selectedProducts.reduce((s,p)=>s+p.price*p.quantity,0);
        const envio = subtotal >= LIMITE_ENVIO_GRATIS ? 0 : COSTO_ENVIO;
        const total = subtotal + envio;

        if (!/^\d{10}$/.test(phone)) {
            alert('Teléfono inválido de 10 dígitos');
            resetFinalize();
            return;
        }
        if (!selectedProducts.length) {
            alert("Carrito vacío");
            resetFinalize();
            return;
        }

        const orderData = { customer:{fullName,email,phone,address}, cart:selectedProducts, totalAmount:total };

        try {
            const res = await fetch('/api/checkout',{
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify(orderData)
            });
            const data = await res.json();
            alert(data.success ? 'Compra exitosa' : (data.message || 'Hubo un problema'));
            if (data.success) {
                localStorage.removeItem('cartData');
                checkoutForm.reset();
                selectedProducts=[];
                updateCart();
                checkoutModal.style.display="none";
                cartDropdown.style.display="none";
            }
        } catch(e){
            console.error(e);
            alert('Error en servidor, intenta de nuevo');
        } finally{ resetFinalize(); }
    });
}

function resetFinalize() {
    isProcessing=false;
    finalizeButton.disabled=false;
    finalizeButton.textContent="Finalizar Compra";
}

function validateForm(form){
    let valid=true;
    form.querySelectorAll('input,textarea').forEach(input=>{
        if(!input.checkValidity()){ input.classList.add('is-invalid'); valid=false; }
        else input.classList.remove('is-invalid');
    });
    return valid;
}

// Cargar carrito desde localStorage al inicio
loadCart();

}