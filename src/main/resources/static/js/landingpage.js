import {isTokenExpired} from "./securityMethods.js";

const app = document.getElementById("app")



export async function loadLandingPage() {
    app.innerHTML = "";

    // --- HEADER ---
    const header = document.createElement("div");
    header.classList.add("header");

    // Logo
    const logo = document.createElement("img");
    logo.src = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/Bitcoin.svg/183px-Bitcoin.svg.png";
    logo.alt = "Crypto Logo";
    logo.classList.add("header-logo");
    header.appendChild(logo);

    // Button holder
    const headerButtonHolder = document.createElement("div");
    headerButtonHolder.classList.add("header-buttons");

    if (isTokenExpired()) {
        const loginSignUpBtn = document.createElement("button");
        loginSignUpBtn.textContent = "Login / Signup";
        loginSignUpBtn.classList.add("header-btn", "login-btn");
        headerButtonHolder.appendChild(loginSignUpBtn);
    } else {
        const goToDashBoardButton = document.createElement("button");
        goToDashBoardButton.textContent = "Dashboard";
        goToDashBoardButton.classList.add("header-btn", "dashboard-btn");
        headerButtonHolder.appendChild(goToDashBoardButton);
    }

    header.appendChild(headerButtonHolder);
    app.appendChild(header);


    // --- HERO SECTION ---

    const heroSection = document.createElement("div");
    heroSection.classList.add("hero-section");


        // Big text
    const heroTitle = document.createElement("h1");
    heroTitle.textContent = "Gustavo Investor Bot";
    heroTitle.classList.add("hero-title");
// crypto wheel
    heroSection.appendChild(heroTitle);

        // Call-to-action button
    const ctaButton = document.createElement("button");
    ctaButton.classList.add("cta-button");

    if (isTokenExpired()) {
        ctaButton.textContent = "Start Today";
        ctaButton.classList.add("start-btn");
    } else {
        ctaButton.textContent = "Go to Dashboard";
        ctaButton.classList.add("dashboard-btn");
    }

    //div til hjul og description
    const midHeroDiv = document.createElement("div")
    midHeroDiv.classList.add("mid-hero-div")
    const cryptoWheel = await create3DCryptoWheel();
    midHeroDiv.appendChild(cryptoWheel)
    const description = document.createElement("p")
    description.textContent = "Indtast din Binance api-nøgle → lad Gustavo handle for dig. Automatisk. Intelligent. 100% hands-free. 🔥 💸 😱";
    midHeroDiv.appendChild(description)


// Append

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

    // --- Fetch cryptos ---
    const response = await fetch("http://localhost:8080/api/cryptos/frontenddto");
    const cryptoList = await response.json();

    // --- Create a card for each crypto ---
    cryptoList.forEach((crypto) => {
        const card = document.createElement("div");
        card.classList.add("coin-card");
        card.dataset.symbol = crypto.ticker.toLowerCase(); // ✅ Lowercase match for Binance

        card.innerHTML = `
      <img src="${crypto.img || 'https://via.placeholder.com/64'}" alt="${crypto.name}" />
      <span class="coin-symbol">${crypto.name}</span>
      <span class="coin-price">$0.00</span>
    `;
        wheel.appendChild(card);
    });

    // --- WebSocket connection ---
    const streams = cryptoList
        .map((c) => `${c.ticker.toLowerCase()}@ticker`)
        .join("/");

    const ws = new WebSocket(`wss://stream.binance.com:9443/stream?streams=${streams}`);

    // --- Live updates ---
    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        const info = data.data;

        if (!info || !info.s) return;

        const symbol = info.s.toLowerCase(); // ✅ Binance always sends uppercase symbols
        const price = parseFloat(info.c).toPrecision(6);

        const card = wheel.querySelector(`[data-symbol="${symbol}"]`);
        if (card) {
            const priceEl = card.querySelector(".coin-price");
            priceEl.textContent = `$${price}`;
        }
    };

    // Optional reconnect
    ws.onclose = () => {
        console.warn("WebSocket closed, reconnecting...");
        setTimeout(create3DCryptoWheel, 5000);
    };

    return wheelContainer;
}

