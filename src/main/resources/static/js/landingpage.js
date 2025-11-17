import {isTokenExpired} from "./securityMethods.js";
import {createLoginModule} from "./loginmodule.js";
import {createLogoutButton, showPopupMessage} from "./reusableFunctions.js";
import {createAskForApiKeys} from "./apiKeysForm.js";

const app = document.getElementById("app")

export async function loadLandingPage(popUpMsg) {
    const oldOverlay = document.querySelector(".login-overlay");
    if (oldOverlay) oldOverlay.remove();
    app.innerHTML = "";

    // Viser en kort besked hvis siden blev åbnet med en notifikation
    if (popUpMsg){
        showPopupMessage(popUpMsg)
    }

    // --- TOPSEKTION (HEADER) ---
    const header = document.createElement("div");
    header.classList.add("header");

    // Logo i venstre side
    const logo = document.createElement("img");
    logo.src = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/Bitcoin.svg/183px-Bitcoin.svg.png";
    logo.alt = "Crypto Logo";
    logo.classList.add("header-logo");
    header.appendChild(logo);

    // Holder til knapper i højre side
    const headerButtonHolder = document.createElement("div");
    headerButtonHolder.classList.add("header-buttons");

    // Hvis ikke logget ind → vis Login/Signup
    if (isTokenExpired()) {
        await createLoginModule();
        const loginSignUpBtn = document.createElement("button");
        loginSignUpBtn.textContent = "Login / Signup";
        loginSignUpBtn.classList.add("header-btn", "login-btn");
        loginSignUpBtn.addEventListener("click", async () => {
            await createLoginModule();
            window.openLoginPopup();
        });
        headerButtonHolder.appendChild(loginSignUpBtn);

        // Hvis logget ind → vis Dashboard + Logout
    } else {
        const goToDashBoardButton = document.createElement("button");
        goToDashBoardButton.textContent = "Dashboard";
        goToDashBoardButton.classList.add("header-btn", "dashboard-btn");
        goToDashBoardButton.addEventListener("click", async function(){
            return await createAskForApiKeys();
        });

        const logoutButton = createLogoutButton()
        headerButtonHolder.appendChild(goToDashBoardButton);
        headerButtonHolder.appendChild(logoutButton);
    }

    header.appendChild(headerButtonHolder);
    app.appendChild(header);

    // --- HERO-SEKTION MED TITEL OG KNAP ---
    const heroSection = document.createElement("div");
    heroSection.classList.add("hero-section");

    // Titel i toppen
    const heroTitle = document.createElement("h1");
    heroTitle.textContent = "Gustavo Investor Bot";
    heroTitle.classList.add("hero-title");
    heroSection.appendChild(heroTitle);

    // --- Midtersektion med hjul + beskrivelse ---
    const midHeroDiv = document.createElement("div")
    midHeroDiv.classList.add("mid-hero-div")

    // 3D crypto-hjulet
    const cryptoWheel = await create3DCryptoWheel();
    midHeroDiv.appendChild(cryptoWheel)

    // Lille forklaring om hvad siden gør
    const description = document.createElement("p")
    description.textContent = "Indtast din Binance api-nøgle → lad Gustavo handle for dig. Automatisk. Intelligent. 100% hands-free. 🔥 💸 😱";
    midHeroDiv.appendChild(description)

    // --- CTA-knap (forskellig om man er logget ind eller ej) ---
    const ctaButton = document.createElement("button");
    ctaButton.classList.add("cta-button");

    if (isTokenExpired()) {
        // Ikke logget ind → knap åbner login-popup
        ctaButton.textContent = "Start Today";
        ctaButton.classList.add("start-btn");
        ctaButton.addEventListener("click", async () => {
            await createLoginModule();
            window.openLoginPopup();
        });

    } else {
        // Logget ind → knap sender til dashboard
        ctaButton.textContent = "Go to Dashboard";
        ctaButton.classList.add("dashboard-btn");
        ctaButton.addEventListener("click", async function(){
            return await createAskForApiKeys();
        });
    }

    heroSection.appendChild(midHeroDiv)
    heroSection.appendChild(ctaButton);
    app.appendChild(heroSection);
}

export async function create3DCryptoWheel() {
    const wheelContainer = document.createElement("div");
    wheelContainer.classList.add("crypto-wheel");

    const wheel = document.createElement("div");
    wheel.classList.add("wheel");
    wheelContainer.appendChild(wheel);

    // Henter kryptovalutaer fra backend
    const response = await fetch("http://localhost:8080/api/cryptos/frontenddto");
    const cryptoList = await response.json();

    // Opret et lille kort for hver coin
    cryptoList.forEach((crypto) => {
        const card = document.createElement("div");
        card.classList.add("coin-card");
        card.dataset.symbol = crypto.ticker.toLowerCase();

        card.innerHTML = `
            <img src="${crypto.img || 'https://via.placeholder.com/64'}" alt="${crypto.name}" />
            <span class="coin-symbol">${crypto.name}</span>
            <span class="coin-price">$0.00</span>
        `;
        wheel.appendChild(card);
    });

    // Opret websocket-stream til live priser
    const streams = cryptoList
        .map((c) => `${c.ticker.toLowerCase()}@ticker`)
        .join("/");

    const ws = new WebSocket(`wss://stream.binance.com:9443/stream?streams=${streams}`);

    // Opdater priser live i hjulet
    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        const info = data.data;

        if (!info || !info.s) return;

        const symbol = info.s.toLowerCase();
        const price = parseFloat(info.c).toPrecision(6);

        const card = wheel.querySelector(`[data-symbol="${symbol}"]`);
        if (card) {
            const priceEl = card.querySelector(".coin-price");
            priceEl.textContent = `$${price}`;
        }
    };

    // Genopret forbindelse hvis streamen falder ud
    ws.onclose = () => {
        console.warn("WebSocket lukket – forsøger igen...");
        setTimeout(create3DCryptoWheel, 5000);
    };

    return wheelContainer;
}
