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

                .holo-container {
                    position: relative;
                    width: 220px;
                    height: 220px;
                    filter: drop-shadow(0 0 25px #00fff5);
                }

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

                /* LOGO HOLOGRÁFICO */
                img.maya-symbol.logo {
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);

                    width: 92px;
                    height: 92px;
                    object-fit: contain;

                    opacity: 0.95;

                    filter:
                        drop-shadow(0 0 6px rgba(0,255,240,0.6))
                        drop-shadow(0 0 14px rgba(0,255,255,0.75))
                        drop-shadow(0 0 28px rgba(0,200,255,0.35));

                    animation:
                        symbolGlow 2.6s ease-in-out infinite,
                        hologramShift 1.2s steps(2, end) infinite,
                        logoPulse 3.2s ease-in-out infinite;
                }

                @keyframes symbolGlow {
                    0%,100% {
                        opacity: .85;
                        filter:
                            drop-shadow(0 0 6px rgba(0,255,240,0.6))
                            drop-shadow(0 0 14px rgba(0,255,255,0.75))
                            drop-shadow(0 0 28px rgba(0,200,255,0.35));
                    }
                    50% {
                        opacity: 1;
                        filter:
                            drop-shadow(0 0 10px rgba(0,255,255,0.9))
                            drop-shadow(0 0 24px rgba(0,255,255,0.8))
                            drop-shadow(0 0 40px rgba(0,255,255,0.45));
                    }
                }

                @keyframes hologramShift {
                    0%   { transform: translate(-50%, -50%) translateX(0); }
                    50%  { transform: translate(-50%, -50%) translateX(1px); }
                    100% { transform: translate(-50%, -50%) translateX(0); }
                }

                @keyframes logoPulse {
                    0%,100% {
                        transform: translate(-50%, -50%) scale(1);
                        opacity: 0.88;
                    }
                    50% {
                        transform: translate(-50%, -50%) scale(1.07);
                        opacity: 1;
                    }
                }

                .loader-text {
                    margin-top: 25px;
                    font-size: 22px;
                    color: #00ffea;
                    text-shadow: 0 0 12px #00ffe7;
                    font-family: 'Poppins', sans-serif;
                    letter-spacing: 1px;
                }
            </style>

            <div class="holo-container">
                <div class="god-rays"></div>

                <div class="ring main"></div>
                <div class="ring inner"></div>
                <div class="ring glass"></div>

                <img
                    class="maya-symbol logo"
                    src="/assets/imgs/logoweed.png"
                    alt="WeedTlanMx Logo"
                />
            </div>

            <p class="loader-text">Cargando nuestros productos...</p>
        `;
    }

    static get observedAttributes() {
        return ["active"];
    }

    attributeChangedCallback(name, oldV, newV) {
        // Solo controla visibilidad
    }
}

customElements.define("maya-loader", MayaLoader);
