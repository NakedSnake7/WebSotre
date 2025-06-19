export function configurarBotonWhatsApp() {
    const whatsappButton = document.getElementById('whatsappButton');
    const phoneNumber = "2213576114";
    const message = "Hola, necesito ayuda con mi compra en WeedTlan";

    if (whatsappButton) {
        whatsappButton.innerHTML = `
            <a href="https://wa.me/${phoneNumber}?text=${encodeURIComponent(message)}" target="_blank" class="btn btn-success">
                <i class="ti-whatsapp" style="font-size: 24px; color: #25D366;"></i> WhatsApp
            </a>
        `;
    }
}
