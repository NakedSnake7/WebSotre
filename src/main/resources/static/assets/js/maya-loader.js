class MayaLoader extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: "open" });

        this.shadowRoot.innerHTML = `
            <style>
                :host {
                    position: fixed;
                    inset: 0;
                    display: none;
                    justify-content: center;
                    align-items: center;
                    flex-direction: column;
                    z-index: 9999;
                    background: radial-gradient(circle at center, rgba(0,40,50,0.78), rgba(0,0,0,0.92));
                    backdrop-filter: blur(10px) brightness(1.1);
                }

                :host([active]) {
                    display: flex;
                }

                /* CONTENEDOR PRINCIPAL */
                .holo-container {
                    position: relative;
                    width: 220px; height: 220px;
                    filter: drop-shadow(0 0 25px #00fff5);
                }

                /* CAPA DE LUZ VOLUMÉTRICA REAL */
                .god-rays {
                    position: absolute;
                    inset: -40px;
                    background: conic-gradient(
                        from 0deg,
                        rgba(0,255,230,0.18),
                        rgba(0,255,200,0.0) 30%,
                        rgba(0,255,230,0.18) 60%,
                        rgba(0,255,200,0.0)
                    );
                    animation: raysSpin 12s linear infinite;
                    filter: blur(12px);
                    border-radius: 50%;
                }

                @keyframes raysSpin {
                    from { transform: rotate(0deg); }
                    to   { transform: rotate(360deg); }
                }

                /* AROS PRINCIPALES */
                .ring {
                    position: absolute;
                    inset: 0;
                    border-radius: 50%;
                }

                .ring.main {
                    border: 4px solid rgba(0,255,245,0.8);
                    box-shadow: 
                        0 0 25px #00fff0,
                        0 0 55px rgba(0,255,255,0.4) inset;
                    animation: mainSpin 4s cubic-bezier(.6,.15,.45,.9) infinite;
                }

                @keyframes mainSpin {
                    from { transform: rotate(0deg); }
                    to   { transform: rotate(360deg); }
                }

                .ring.inner {
                    inset: 28px;
                    border: 2px dashed rgba(0,255,200,0.5);
                    animation: innerSpin 6.8s linear infinite reverse;
                }

                @keyframes innerSpin {
                    from { transform: rotate(360deg); opacity: .8; }
                    to   { transform: rotate(0deg); opacity: .5; }
                }

                /* ANILLO GLASS */
                .ring.glass {
                    inset: 45px;
                    border: 3px solid rgba(0,255,255,0.35);
                    backdrop-filter: blur(3px);
                    animation: glassPulse 3s ease-in-out infinite;
                }

                @keyframes glassPulse {
                    0%,100% { opacity: .45; transform: scale(1); }
                    50%     { opacity: .75; transform: scale(1.08); }
                }

                /* SÍMBOLO MAYA HOLOGRÁFICO */
                svg.maya-symbol {
                    position: absolute;
                    top: 50%; left: 50%;
                    transform: translate(-50%, -50%);
                    filter: drop-shadow(0 0 8px #00fff0);
                    animation: symbolGlow 2.4s ease-in-out infinite,
                               hologramShift 1.2s steps(2, end) infinite;
                }

                @keyframes symbolGlow {
                    0%,100% { opacity: .85; filter: drop-shadow(0 0 8px #00fff0); }
                    50%     { opacity: 1; filter: drop-shadow(0 0 18px #00ffff); }
                }

                @keyframes hologramShift {
                    0% { transform: translate(-50%, -50%) translateX(0px); }
                    50% { transform: translate(-50%, -50%) translateX(1px); }
                    100% { transform: translate(-50%, -50%) translateX(0px); }
                }

                /* TEXTO */
                .loader-text {
                    margin-top: 25px;
                    font-size: 22px;
                    color: #00ffea;
                    text-shadow: 0 0 12px #00ffe7;
                    font-family: 'Poppins', sans-serif;
                    letter-spacing: 1px;
                }
            </style>

            <!-- AUDIO -->
			<audio id="portalSound" src="/assets/sounds/portal_multidimensional.wav" preload="auto" loop></audio>



            <div class="holo-container">
                <div class="god-rays"></div>

                <div class="ring main"></div>
                <div class="ring inner"></div>
                <div class="ring glass"></div>

                <svg class="maya-symbol" width="90" height="90" viewBox="0 0 100 100">
                    <path d="M50 10 L80 30 L80 60 L50 90 L20 60 L20 30 Z"
                        fill="none" stroke="#00fff7" stroke-width="4" stroke-linejoin="round"/>
                    <circle cx="50" cy="50" r="20"
                        fill="none" stroke="#00eaff" stroke-width="3"/>
                    <circle cx="50" cy="50" r="7"
                        fill="#00ffea" stroke="#00fff7" stroke-width="2"/>
                </svg>
            </div>

            <p class="loader-text">Cargando nuestros productos...</p>
        `;
    }

    static get observedAttributes() {
        return ["active"];
    }

	attributeChangedCallback(name, oldV, newV) {
	    const sound = this.shadowRoot.getElementById("portalSound");

	    if (name === "active") {
	        if (newV !== null) {
	            // Iniciar con volumen 0 y hacer fade in
	            sound.volume = 0;
	            sound.currentTime = 0;
	            sound.play().catch(() => {});

	            const targetVolume = 0.36;
	            const fadeIn = setInterval(() => {
	                if (sound.volume < targetVolume - 0.01) {
	                    sound.volume += 0.02; // sube gradualmente
	                } else {
	                    sound.volume = targetVolume;
	                    clearInterval(fadeIn);
	                }
	            }, 50); // cada 50ms
	        } else {
	            // Fade out suave
	            const fadeOut = setInterval(() => {
	                if (sound.volume > 0.02) {
	                    sound.volume -= 0.02; // baja gradualmente
	                } else {
	                    sound.volume = 0;
	                    sound.pause();
	                    sound.currentTime = 0;
	                    clearInterval(fadeOut);
	                }
	            }, 50);
	        }
	    }
	}

}

customElements.define("maya-loader", MayaLoader);
