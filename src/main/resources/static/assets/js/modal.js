export function configurarModal() {
    const modalElement = document.getElementById("modal");
    const bsModal = modalElement ? new bootstrap.Modal(modalElement) : null;

    const ageCheck = document.getElementById("ageCheck");
    const submitBtn = document.getElementById("submitBtn");
    const subscribeForm = document.getElementById("subscribeForm");
    const closeModalBtn = document.getElementById("closeModalBtn");

    // 📌 Mostrar automáticamente si NO está registrado y NO lo cerró manualmente
    if (!localStorage.getItem("usuarioRegistrado") &&
        !sessionStorage.getItem("modalClosed") &&
        bsModal) {

        setTimeout(() => bsModal.show(), 2000);
    }

    // 📌 Habilitar botón cuando marque el checkbox
    if (ageCheck && submitBtn) {
        ageCheck.addEventListener("change", () => {
            submitBtn.disabled = !ageCheck.checked;
        });
    }

    // 📌 Acción del botón cerrar
    if (closeModalBtn) {
        closeModalBtn.addEventListener("click", () => {
            bsModal.hide();
            sessionStorage.setItem("modalClosed", "true");
        });
    }

    // 📌 Enviar formulario
    if (subscribeForm) {
        subscribeForm.addEventListener("submit", function (event) {
            event.preventDefault();

            if (!ageCheck.checked) {
                alert("Debes confirmar que eres mayor de 21 años.");
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = "Registrando...";

            const fullName = document.getElementById("Costumer").value.trim();
            const email = document.getElementById("email").value.trim();

            fetch("/api/subscribe", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ fullName, email })
            })
            .then(response => response.json())
            .then(data => {
                alert(data.message);
                localStorage.setItem("usuarioRegistrado", "true");

                bsModal.hide();
                sessionStorage.setItem("modalClosed", "true");
            })
            .catch(error => console.error("Error:", error));
        });
    }
}
