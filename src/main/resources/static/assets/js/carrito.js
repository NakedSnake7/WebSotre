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

    let selectedProducts = [];
    let isProcessing = false;

    function saveCartToLocalStorage() {
        localStorage.setItem('cartData', JSON.stringify({
            products: selectedProducts,
            total: selectedProducts.reduce((sum, p) => sum + p.price * p.quantity, 0)
        }));
    }

    function loadCartFromLocalStorage() {
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
	    
	    const LIMITE_ENVIO_GRATIS = 1250;
	    const COSTO_ENVIO = 120;

	    let subtotal = 0;

	    selectedProducts.forEach(product => {
	        subtotal += product.price * product.quantity;
	        const li = document.createElement('li');
	        li.classList.add('d-flex', 'justify-content-between', 'align-items-center', 'mb-2');
	        li.innerHTML = `
	            <div><b>${product.name}</b> - $${product.price} x ${product.quantity}</div>
	            <div class="d-flex align-items-center">
	                <input type="number" class="remove-quantity" min="1" max="${product.quantity}" value="1" data-product="${product.name}" style="width: 3rem; text-align: center; margin-right: 5px;">
	                <button class="btn btn-danger btn-sm remove-button" data-product="${product.name}">Eliminar</button>
	            </div>
	        `;
	        cartItems.appendChild(li);
	    });

	    const envio = subtotal >= LIMITE_ENVIO_GRATIS ? 0 : COSTO_ENVIO;
	    const totalFinal = subtotal + envio;

	    // Mostrar en resumen de modal y dropdown
	    if (cartTotal) cartTotal.textContent = `$${totalFinal.toFixed(2)}`;
	    if (modalTotal) modalTotal.textContent = `$${totalFinal.toFixed(2)}`;

	    // Guardar en localStorage incluyendo totales
	    localStorage.setItem('cartData', JSON.stringify({
	        products: selectedProducts,
	        subtotal: subtotal,
	        envio,
	        total: totalFinal
	    }));
		// Mostrar barra de progreso de envío gratis
		const envioGratisMensaje = document.getElementById('envioGratisMensaje');
		const envioGratisBarra = document.getElementById('envioGratisBarra');
		const envioGratisContainer = document.getElementById('envioGratisContainer');

		if (envioGratisContainer && envioGratisMensaje && envioGratisBarra) {
		    if (selectedProducts.length === 0) {
		        envioGratisContainer.style.display = 'none';
		    } else {
		        envioGratisContainer.style.display = 'block';

		        if (subtotal >= LIMITE_ENVIO_GRATIS) {
		            envioGratisMensaje.textContent = "🎉 ¡Tienes envío gratis!";
		            envioGratisBarra.style.width = "100%";
		            envioGratisBarra.setAttribute("aria-valuenow", "100");
		        } else {
		            const faltante = LIMITE_ENVIO_GRATIS - subtotal;
		            const progreso = (subtotal / LIMITE_ENVIO_GRATIS) * 100;
		            envioGratisMensaje.textContent = `Agrega $${faltante.toFixed(2)} más para obtener envío gratis`;
		            envioGratisBarra.style.width = `${progreso.toFixed(0)}%`;
		            envioGratisBarra.setAttribute("aria-valuenow", progreso.toFixed(0));
		        }
		    }
		}

	    if (cartDropdown) cartDropdown.style.display = selectedProducts.length > 0 ? 'block' : 'none';
		const resumenDesglose = document.getElementById('cartResumenDesglose');
		if (resumenDesglose) {
		    resumenDesglose.innerHTML = `
		        <div><b>Subtotal:</b> $${subtotal.toFixed(2)}</div>
		        <div><b>Envío:</b> $${envio.toFixed(2)}</div>
		    `;
		}
		const modalResumenDesglose = document.getElementById('modalResumenDesglose');
		if (modalResumenDesglose) {
		    modalResumenDesglose.innerHTML = `
		        <div><b>Subtotal:</b> $${subtotal.toFixed(2)}</div>
		        <div><b>Envío:</b> $${envio.toFixed(2)}</div>
		    `;
		}

	}


    function addToCart(name, price, quantityId) {
        const quantityInput = document.getElementById(quantityId);
        const quantity = parseInt(quantityInput?.value) || 0;
        if (quantity <= 0) return alert('Cantidad inválida.');

        const existing = selectedProducts.find(p => p.name === name);
        if (existing) existing.quantity += quantity;
        else selectedProducts.push({ name, price, quantity });

        updateCart();
    }

    function removeFromCart(name, qty) {
        const product = selectedProducts.find(p => p.name === name);
        if (product) {
            product.quantity -= qty;
            if (product.quantity <= 0) {
                selectedProducts = selectedProducts.filter(p => p.name !== name);
            }
        }
        updateCart();
    }

    if (cartItems) {
        cartItems.addEventListener('click', function (event) {
            if (event.target.classList.contains('remove-button')) {
                const name = event.target.getAttribute('data-product');
                const qty = parseInt(event.target.previousElementSibling.value);
                if (qty > 0) removeFromCart(name, qty);
            }
        });
    }

    if (cartButton && cartDropdown) {
        cartButton.addEventListener('click', () => {
            cartDropdown.style.display = cartDropdown.style.display === 'block' ? 'none' : 'block';
        });
    }

    if (checkoutButton && checkoutModal) {
        checkoutButton.addEventListener('click', () => {
            checkoutModal.style.display = 'block';
        });
    }

    if (finalizeButton && checkoutForm) {
        finalizeButton.addEventListener("click", function () {
            if (isProcessing) return;
            isProcessing = true;

            if (!validateForm(checkoutForm)) {
                alert('Por favor, corrige los errores en el formulario.');
                isProcessing = false;
                return;
            }

            const fullName = document.getElementById('fullName').value.trim();
            const email = document.getElementById('CosEmail').value.trim();
            const phone = document.getElementById('phone').value.trim();
            const address = document.getElementById('address').value.trim();
            const total = parseFloat(modalTotal.innerText.replace("$", "").trim());

            if (!/^\d{10}$/.test(phone)) {
                alert('¡Por favor, ingresa un teléfono válido de 10 dígitos!');
                isProcessing = false;
                return;
            }

            if (isNaN(total) || total <= 0) {
                alert("¡El Carrito Está Vacío!");
                isProcessing = false;
                return;
            }

            const orderData = {
                customer: { fullName, email, phone, address },
                cart: selectedProducts,
                totalAmount: total
            };

            fetch('/api/checkout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(orderData)
            })
            .then(res => res.json())
            .then(data => {
                alert(data.success ? '¡Compra realizada con éxito!' : (data.message || 'Hubo un problema.'));
                if (data.success) {
                    localStorage.removeItem('cartData');
                    checkoutForm.reset();
                    selectedProducts = [];
                    updateCart();
                    checkoutModal.style.display = "none";
                    cartDropdown.style.display = "none";
                }
            })
            .catch(err => {
                console.error('Error en la compra:', err);
                alert('Error en el servidor.');
            })
            .finally(() => {
                isProcessing = false;
                finalizeButton.disabled = false;
                finalizeButton.textContent = "Finalizar Compra";
            });
        });
    }

    function validateForm(form) {
        const inputs = form.querySelectorAll('input, textarea');
        let valid = true;
        inputs.forEach(input => {
            if (!input.checkValidity()) {
                input.classList.add('is-invalid');
                valid = false;
            } else {
                input.classList.remove('is-invalid');
            }
        });
        return valid;
    }

    document.querySelectorAll('.add-to-cart').forEach(button => {
        button.addEventListener('click', function () {
            const name = this.getAttribute('data-name');
            const price = parseFloat(this.getAttribute('data-price'));
            const quantityInputId = this.getAttribute('data-quantity-id');
            addToCart(name, price, quantityInputId);
        });
    });

    loadCartFromLocalStorage();
}
