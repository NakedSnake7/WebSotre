export function configurarModal() {
    const modal = document.getElementById("modal");
    const ageCheck = document.getElementById("ageCheck");
    const submitBtn = document.getElementById("submitBtn");
    const subscribeForm = document.getElementById("subscribeForm");

    if (!localStorage.getItem("usuarioRegistrado") && modal) {
        setTimeout(() => modal.style.display = "block", 2000);
    }

    if (ageCheck && submitBtn) {
        ageCheck.addEventListener("change", () => submitBtn.disabled = !ageCheck.checked);
    }

    if (subscribeForm) {
        subscribeForm.addEventListener("submit", function (event) {
            event.preventDefault();

            if (!ageCheck.checked) {
                alert("Debes confirmar que eres mayor de 21 años.");
                return;
            }

            const submitButton = submitBtn;
            submitButton.disabled = true;
            submitButton.textContent = 'Registrando...';

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
                cerrarModal();
                sessionStorage.setItem("modalClosed", "true");
            })
            .catch(error => console.error("Error:", error));
        });
    }

    function cerrarModal() {
        if (!modal) return;
        modal.style.display = "none";
        document.body.classList.remove("modal-open");
        document.querySelectorAll(".modal-backdrop").forEach(el => el.remove());
        subscribeForm.reset();
        submitBtn.disabled = true;
    }
}
