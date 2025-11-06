const token = localStorage.getItem("token");
import {authorizedFetch} from "./securityMethods.js";
document.addEventListener("DOMContentLoaded", () => {
    const app = document.getElementById("app");
    renderLandingPage(app);
});

function createElement(tag, attrs = {}, children = []) {
    const el = document.createElement(tag);
    Object.entries(attrs).forEach(([key, val]) => {
        if (key === "class") el.className = val;
        else if (key === "text") el.textContent = val;
        else el.setAttribute(key, val);
    });
    children.forEach(child => {
        if (typeof child === "string") el.appendChild(document.createTextNode(child));
        else if (child instanceof Node) el.appendChild(child);
    });
    return el;
}

async function renderLandingPage(app) {
    app.innerHTML = ""; // clear old content

    // Navbar
    const logo = createElement("div", { class: "logo", text: "CryptoAIInvestor" });
    const loginBtn = createElement("button", { id: "loginBtn", class: "btn-secondary", text: "Login" });
    const startBtn = createElement("button", { id: "startBtn", class: "btn-primary", text: "Get Started" });
    startBtn.addEventListener("click",async function(){

      const response =  await authorizedFetch("http://localhost:8080/api/auth/me")

        console.log(await response.json())

    })
    const nav = createElement("nav", { class: "nav-links" }, [loginBtn, startBtn]);
    const header = createElement("header", { class: "navbar" }, [logo, nav]);

    // Hero
    const heroTitle = createElement("h1", { text: "The future of crypto investing." });
    const heroText = createElement("p", { text: "AI-powered insights. Smarter trades. Automated strategies. Your crypto journey starts here." });
    const ctaBtn = createElement("button", { id: "ctaBtn", class: "btn-primary", text: "Start Trading" });
    const heroContent = createElement("div", { class: "hero-content" }, [heroTitle, heroText, ctaBtn]);
    const cryptoDisplay = createElement("div", { class: "cryptoPriceDisplayer" });
    const heroSection = createElement("section", { class: "hero" }, [heroContent, cryptoDisplay]);

    // Footer
    const footerText = createElement("p", { text: "© 2025 Crypto AI Investor" });
    const footer = createElement("footer", {}, [footerText]);

    // Modal
    const modal = createElement("div", { id: "loginModal", class: "modal hidden" });
    const modalContent = createElement("div", { class: "modal-content" });
    const modalTitle = createElement("h2", { text: "Login" });
    const form = createElement("form", { id: "loginForm" });
    const usernameInput = createElement("input", { type: "text", id: "username", placeholder: "Username", required: true });
    const passwordInput = createElement("input", { type: "password", id: "password", placeholder: "Password", required: true });
    const submitBtn = createElement("button", { type: "submit", class: "btn-primary", text: "Login" });
    const cancelBtn = createElement("button", { type: "button", id: "closeModal", class: "btn-secondary", text: "Cancel" });
    form.append(usernameInput, passwordInput, submitBtn, cancelBtn);
    modalContent.append(modalTitle, form);
    modal.append(modalContent);

    // Append everything
    app.append(header, heroSection, footer, modal);

    // Init features
    startLivePrices();
    setupModal(loginBtn, modal);
    setupLoginForm(form, modal);
}

function setupModal(loginBtn, modal) {
    const closeBtn = modal.querySelector("#closeModal");

    loginBtn.addEventListener("click", () => modal.classList.remove("hidden"));
    closeBtn.addEventListener("click", () => modal.classList.add("hidden"));
    modal.addEventListener("click", (e) => {
        if (e.target === modal) modal.classList.add("hidden");
    });
}

function setupLoginForm(form, modal) {
    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const username = form.querySelector("#username").value;
        const password = form.querySelector("#password").value;

        try {
            const res = await fetch("http://localhost:8080/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password }),
            });

            if (!res.ok) {
                alert("❌ Invalid login");
                return;
            }

            const data = await res.json();
            localStorage.setItem("token", data.token);
            alert("✅ Logged in!");
            modal.classList.add("hidden");
        } catch (err) {
            console.error(err);
            alert("⚠️ Server error");
        }
    });
}

function startLivePrices() {
    const container = document.querySelector(".cryptoPriceDisplayer");
    const symbols = ["btcusdt", "ethusdt", "xrpusdt", "bnbusdt", "solusdt", "trxusdt", "dogeusdt", "adausdt", "avaxusdt", "linkusdt"];

    const wsUrl = `wss://stream.binance.com:9443/stream?streams=${symbols.map(s => `${s}@ticker`).join("/")}`;
    const ws = new WebSocket(wsUrl);

    symbols.forEach(symbol => {
        const card = createElement("div", { class: "cryptoCard", id: symbol });
        const title = createElement("h3", { text: symbol.toUpperCase() });
        const price = createElement("p", { class: "price", text: "Loading..." });
        card.append(title, price);
        container.append(card);
    });

    ws.onmessage = (event) => {
        const msg = JSON.parse(event.data);
        const data = msg?.data;
        if (!data) return;

        const symbol = data.s.toLowerCase();
        const el = document.querySelector(`#${symbol} .price`);
        if (el) {
            const price = parseFloat(data.c).toFixed(5);
            el.textContent = `$${price}`;
            el.style.color = data.P >= 0 ? "#4ade80" : "#ef4444";
        }
    };

    ws.onclose = () => {
        console.warn("WebSocket closed. Reconnecting...");
        setTimeout(startLivePrices, 3000);
    };
}
