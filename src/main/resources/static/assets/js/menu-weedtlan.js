class MenuWeedTlan extends HTMLElement {

    connectedCallback() {
        this.loadMenuFragment();
    }

    async loadMenuFragment() {
        try {
            // Cargar el fragmento Thymeleaf exactamente igual que antes
            const resp = await fetch("/fragmento-menu");
            const html = await resp.text();

            // Insertar HTML
            this.innerHTML = html;

            // Ejecutar scripts dentro del fragmento (Bootstrap, etc.)
            this.runScripts();

            // Esperar siguiente frame para asegurar render visual
            requestAnimationFrame(() => {
                this.dispatchEvent(new CustomEvent("menu-ready", {
                    bubbles: true,
                    composed: true
                }));
            });

        } catch (error) {
            console.error("Error cargando fragmento del menú:", error);

            this.dispatchEvent(new CustomEvent("menu-error", {
                bubbles: true,
                composed: true
            }));
        }
    }

    runScripts() {
        const scripts = this.querySelectorAll("script");

        scripts.forEach(oldScript => {
            const newScript = document.createElement("script");

            if (oldScript.src) {
                newScript.src = oldScript.src;
            } else {
                newScript.textContent = oldScript.textContent;
            }

            document.body.appendChild(newScript);
            document.body.removeChild(newScript);
        });
    }
}

customElements.define("menu-weedtlan", MenuWeedTlan);
