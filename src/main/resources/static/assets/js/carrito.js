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

	
	let appliedCoupon = null;
	
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
        // Actualizar stock badges al iniciar
        document.querySelectorAll('.add-to-cart').forEach(btn => {
            const quantityId = btn.dataset.quantityId;
            const stock = parseInt(btn.dataset.originalStock);
            updateStockBadge(quantityId, stock);
        });
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
				<input
				  type="number"
				  class="remove-quantity"
				  min="1"
				  max="${p.quantity}"
				  value="1"
				  data-product-id="${p.id}"
				  style="width: 3rem; text-align: center; margin-right: 5px;">
					<button
					  class="btn btn-danger btn-sm remove-button"
					  data-product-id="${p.id}">
					  Eliminar
					</button>

                </div>
            `;
            cartItems.appendChild(li);
        });

        if (cartTotal) cartTotal.textContent = `$${totalFinal.toFixed(2)}`;
        if (modalTotal) modalTotal.textContent = `$${totalFinal.toFixed(2)}`;

        const cartCounter = document.getElementById('cartCounter');
        if (cartCounter) {
            const totalItems = selectedProducts.reduce((sum, p) => sum + p.quantity, 0);
            cartCounter.textContent = totalItems;
        }

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

	// FUNCION: agregar al carrito
	function addToCart(id, name, price, quantityId, originalStock) {
	    if (!id) return alert("Error: ID del producto no definido");

	    const input = document.getElementById(quantityId);
	    const qty = parseInt(input?.value) || 0;
	    if (qty <= 0) return alert('Cantidad inválida');

	    const existing = selectedProducts.find(p => p.id === id);
	    const inCartQty = existing ? existing.quantity : 0;
	    const availableStock = originalStock - inCartQty;
	    if (qty > availableStock) return alert(`Solo hay ${availableStock} unidades disponibles`);

	    if (existing) existing.quantity += qty;
	    else selectedProducts.push({ id, name, price, quantity: qty, quantityId });

	    cartButton.classList.add('cart-animate');
	    setTimeout(() => cartButton.classList.remove('cart-animate'), 500);

	    updateStockBadge(quantityId, originalStock);
	    updateCart();
	}


	// FUNCION: eliminar del carrito
	function removeFromCart(productId, qty) {
	    const existing = selectedProducts.find(p => p.id === productId);
	    if (!existing) return;

	    existing.quantity -= qty;
	    if (existing.quantity <= 0) {
	        // Guardamos quantityId antes de eliminarlo
	        const quantityId = existing.quantityId;
	        selectedProducts = selectedProducts.filter(p => p.id !== productId);
	        // Restaurar badge usando quantityId
			const input = document.getElementById(quantityId);
			if (input) {
				const btn = input.closest('.product-card').querySelector('.add-to-cart'); // botón add-to-cart
			    const originalStock = parseInt(btn.dataset.originalStock);
			    updateStockBadge(quantityId, originalStock);
			}

	    } else {
	        // Si aún queda cantidad, actualizar badge con quantityId
	        const quantityId = existing.quantityId;
			const input = document.getElementById(quantityId);
			if (input) {
			    const btn = input.nextElementSibling; // botón add-to-cart
			    const originalStock = parseInt(btn.dataset.originalStock);
			    updateStockBadge(quantityId, originalStock);
			}

	    }

	    updateCart();
	}

	// FUNCION: actualizar badge y stock
	function updateStockBadge(quantityId, originalStock) {
	    const input = document.getElementById(quantityId);
	    if (!input) return;

	    const productId = Number(input.dataset.productId);

	    const btn = document.querySelector(
	        `.add-to-cart[data-product-id="${productId}"]`
	    );
	    if (!btn) return;

	    // Cantidad actual en carrito (POR ID)
	    const inCartQtyObj = selectedProducts.find(p => p.id === productId);
	    const inCartQty = inCartQtyObj ? inCartQtyObj.quantity : 0;

	    const availableStock = originalStock - inCartQty;

	    // Crear badge si no existe
	    let badge = input.parentElement.querySelector("small");
	    if (!badge) {
	        badge = document.createElement("small");
	        input.parentElement.appendChild(badge);
	    }

	    // Actualizar badge
	    if (availableStock > 10) {
	        badge.className = "ms-2 text-success";
	        badge.textContent = `Stock: ${availableStock}`;
	    } else if (availableStock > 0) {
	        badge.className = "ms-2 text-warning";
	        badge.textContent = `Stock: ${availableStock}`;
	    } else {
	        badge.className = "ms-2 text-danger";
	        badge.textContent = "Agotado";
	    }

	    // Input y botón
	    input.max = Math.max(availableStock, 0);
	    input.disabled = availableStock <= 0;
	    btn.disabled = availableStock <= 0;
	}




    // Delegación de eventos para botones "Agregar al carrito"
	document.body.addEventListener('click', function(e){
	    const btn = e.target.closest('.add-to-cart');
	    if (!btn) return;

        const id = Number(btn.dataset.productId); // 🔥 id como Long		
		const name = btn.getAttribute('data-name');
		const price = parseFloat(btn.getAttribute('data-price'));
		const quantityId = btn.getAttribute('data-quantity-id');
		const stock = parseInt(btn.getAttribute('data-original-stock'));
		
		//borrar despues
		console.log("DATA:", btn.dataset, btn.dataset.productId);

	    addToCart(id, name, price, quantityId, stock);
	});

    // Remover productos del carrito
    if (cartItems) {
        cartItems.addEventListener('click', e => {
			if (e.target.classList.contains('remove-button')) {
			    const productId = Number(e.target.dataset.productId);
			    const qty = parseInt(
			        e.target.previousElementSibling.value
			    ) || 1;

			    removeFromCart(productId, qty);
			}

        });
    }

    // Mostrar/Ocultar carrito
    if (cartButton && cartDropdown) {
        cartButton.addEventListener('click', () => {
            cartDropdown.classList.toggle('open');
        });
    }

	if (checkoutButton && checkoutModal) {
	    checkoutButton.addEventListener('click', () => {
	        $('#checkoutModal').modal('show');
	    });
	}

	// =========================
	// FINALIZAR COMPRA
	// =========================
	if (finalizeButton && checkoutForm) {
	    finalizeButton.addEventListener('click', async (e) => {
	        e.preventDefault();
			
			const paymentMethod = document.querySelector(
			    'input[name="paymentMethod"]:checked'
			)?.value;

			if (isProcessing) return;
            isProcessing = true;


			const loader = document.getElementById("loader");
			  loader.setAttribute("active", ""); // 🔥 ACTIVA EL LOADER
			  loader.shadowRoot.querySelector(".loader-text").textContent = "Procesando tu pedido...";

	       
	        finalizeButton.disabled = true;
	        finalizeButton.textContent = "Procesando...";

	        // Limpiar mensajes antiguos
	        const errorContainer = document.getElementById('checkoutErrors');
	        if (errorContainer) errorContainer.innerHTML = "";

	        // Obtener campos
	        const fullName = document.getElementById('fullName').value.trim();
			const email = document.getElementById('email').value.trim();
	        const phone = document.getElementById('phone').value.trim();
	        const address = document.getElementById('address').value.trim();

	        const errors = [];

	        // Validaciones
	        if (!fullName) errors.push("Ingresa tu nombre completo.");
	        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
	            errors.push("Ingresa un correo electrónico válido.");
	        }
	        if (!/^\d{10}$/.test(phone)) {
	            errors.push("Teléfono inválido. Debe tener 10 dígitos.");
	        }
	        if (!address) errors.push("Ingresa tu dirección de envío.");
	        if (!selectedProducts.length) {
	            errors.push("Tu carrito está vacío.");
	        }
			if (!paymentMethod) {
			    errors.push("Selecciona un método de pago.");
			}


	        // Mostrar errores
	        if (errors.length > 0) {
	            errors.forEach(msg => {
	                const p = document.createElement('p');
	                p.textContent = msg;
	                p.style.color = "#ff6b6b";
	                p.style.margin = "4px 0";
	                errorContainer.appendChild(p);
	            });

	            resetFinalize();
				loader.removeAttribute("active");
				loader.shadowRoot.querySelector(".loader-text").textContent =
				    "Cargando nuestros productos...";
	            return;
	        }

	        // Si todo OK — calcular totales
	        const subtotal = selectedProducts.reduce((s,p)=>s+p.price*p.quantity,0);
	        const envio = subtotal >= LIMITE_ENVIO_GRATIS ? 0 : COSTO_ENVIO;
	        const total = subtotal + envio;

			const orderData = {
			  customer: {
			    fullName,
			    email,
			    phone,
			    address
			  },
			  cart: selectedProducts.map(p => ({
			    productId: p.id,          // 🔥 ID REAL DEL PRODUCTO
			    name: p.name,
			    price: p.price,
			    quantity: p.quantity
			  })),
			  paymentMethod: paymentMethod,
			  couponCode: appliedCoupon ?? null   // ✅ asegura que no falle aunque appliedCoupon sea undefined
			};



			console.log("ORDER DATA ENVIADO:", JSON.stringify(orderData, null, 2));
	        try {
				// ==========================
				// FLUJO SEGÚN MÉTODO DE PAGO
				// ==========================
				
  
				   if (paymentMethod === "TRANSFER") {

				        const res = await fetch('/api/checkout', {
				            method: 'POST',
				            headers: { 'Content-Type': 'application/json' },
				            body: JSON.stringify(orderData)
				        });

				        const data = await res.json();
				        if (!res.ok) {
				            throw new Error(data.message || "Error al crear la orden");
				        }

				        alert("¡Orden creada excitosamente, revisa tu correo!.");

				        localStorage.removeItem('cartData');
				        checkoutForm.reset();
				        selectedProducts = [];
				        updateCart();
				        $('#checkoutModal').modal('hide');
				        cartDropdown.style.display = 'none';

					} else if (paymentMethod === "STRIPE") {

					    const res = await fetch('/api/checkout', {
					        method: 'POST',
					        headers: { 'Content-Type': 'application/json' },
					        body: JSON.stringify(orderData)
					    });

					    const order = await res.json();
					    if (!res.ok) {
							console.error("Respuesta backend:", order);

							throw new Error(
							  order.message ||
							  JSON.stringify(order.errors || order)
							);
					    }

					    const stripeRes = await fetch(`/api/stripe/create-session/${order.orderId}`, {
					        method: 'POST'
					    });

					    const stripeData = await stripeRes.json();
					    if (!stripeRes.ok) {
					        throw new Error(stripeData.error || "Error en Stripe");
					    }

					    window.location.href = stripeData.url;
					    return; // 🔒 CIERRE TOTAL DEL FLUJO
					}

				} catch (e) {
				    console.error(e);
				    alert(e.message || 'Error en servidor, intenta de nuevo');
				}
				
				 
				finally {
				    if (paymentMethod !== "STRIPE") {
				        loader.removeAttribute("active");
				        loader.shadowRoot.querySelector(".loader-text").textContent =
				            "Cargando nuestros productos...";
				        resetFinalize();
				    }
				}

	    }
	);
	}

	// Reset del botón
	function resetFinalize() {
	    isProcessing = false;
	    finalizeButton.disabled = false;
	    finalizeButton.textContent = "Finalizar Compra";
	}



	// ==============================
	// VALIDACIÓN EN TIEMPO REAL + BLUR
	// ==============================
	const realTimeFields = {
	    fullName: {
	        validar: value => value.trim().length > 0,
	        mensaje: "Por favor, ingresa tu nombre completo."
	    },
	    email: {
	        validar: value => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim()),
	        mensaje: "Ingresa un correo válido."
	    },
	    phone: {
	        validar: value => /^\d{10}$/.test(value.trim()),
	        mensaje: "Número inválido: deben ser 10 dígitos."
	    },
	    address: {
			validar: value => {
			    const v = value.trim();

			    if (v.length < 15) return false;
			    if (!/[A-Za-z]{3,}/.test(v)) return false; // mínimo palabras reales
			    if (!/\d{1,5}/.test(v)) return false; // número de casa
			    if (!/[,]/.test(v)) return false; // debe separar elementos con coma

			    return true;
			},
			mensaje: "Incluye calle, número, colonia, CP y ciudad. Ej: Calle 20 #102, Col. Centro, 01109, CDMX"

	    }
	};

	// Asignar eventos input + blur
	Object.keys(realTimeFields).forEach(id => {
	    const campo = document.getElementById(id);

	    campo.addEventListener("input", () => validarCampo(id));
	    campo.addEventListener("blur", () => validarCampo(id));
	});

	function validarCampo(id) {
	    const campo = document.getElementById(id);
	    const regla = realTimeFields[id];
	    const valido = regla.validar(campo.value);
	    const errorDiv = campo.parentElement.querySelector(".invalid-feedback");

		if (!valido) {
		    campo.classList.add("is-invalid");
		    errorDiv.textContent = regla.mensaje;
		    errorDiv.style.display = "block"; // asegura que se muestre
		} else {
		    campo.classList.remove("is-invalid");
		    errorDiv.textContent = "";
		    errorDiv.style.display = "none"; // oculta el mensaje
		}

	}

    // Cargar carrito desde localStorage al inicio
    loadCart();
}
