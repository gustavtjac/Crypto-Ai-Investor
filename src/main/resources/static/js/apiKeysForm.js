import {authorizedFetch, isTokenExpired} from "./securityMethods.js";
import {loadLandingPage} from "./landingpage.js";
import {showPopupMessage} from "./reusableFunctions.js";
import {loadDashBoard} from "./dashboard.js";

const app = document.getElementById("app")



export async function createAskForApiKeys(){
        app.innerHTML= "";
        if (isTokenExpired()){
            return loadLandingPage("Din token er udløbet")
        }

       await checkIfUserHasApiKey()
}

async function checkIfUserHasApiKey() {
    try {
        const response = await authorizedFetch("http://localhost:8080/api/users/checkifapikey");

        if (response.status === 401) {
            return await loadLandingPage("Din session er udløbet. Log ind igen.");
        }

        if (!response.ok) {
            throw new Error("Kunne ikke tjekke API-nøgle");
        }

        const hasApiKey = await response.json();

        if (hasApiKey) {
            console.log("✅ Homie har allerede api nøgle");
           return await loadDashBoard()
        } else {
            console.log("❌ Homie har ikke api nøgle");
            return showAddApiKeyPrompt();
        }

    } catch (error) {
        console.error("Error checking API key:", error);
        await loadLandingPage("Fejl ved hentning af API-nøgle");
    }
}

function showAddApiKeyPrompt() {
    // Clear app content
    app.innerHTML = "";

    // === MAIN CONTAINER (use same translucent style) ===
    const container = document.createElement("div");
    container.classList.add("login-container"); // reuse your glass-style box
    container.style.margin = "6rem auto";       // center it on page
    container.style.maxWidth = "500px";

    // === HEADER ===
    const header = document.createElement("h2");
    header.classList.add("login-header");
    header.textContent = "Tilføj Binance API-nøgle";

    // === INFO TEXT ===
    const info = document.createElement("p");
    info.textContent = "Indtast din offentlige og hemmelige nøgle for at benytte appen";

    // === INPUTS HOLDER ===
    const inputsHolder = document.createElement("div");
    inputsHolder.classList.add("inputs-holder");

    const apiKeyInput = document.createElement("input");
    apiKeyInput.type = "text";
    apiKeyInput.placeholder = "API-nøgle";
    apiKeyInput.classList.add("input-field");
    apiKeyInput.id = "apiKey";

    const secretKeyInput = document.createElement("input");
    secretKeyInput.type = "password";
    secretKeyInput.placeholder = "Secret nøgle";
    secretKeyInput.classList.add("input-field");
    secretKeyInput.id = "secretKey";

    inputsHolder.appendChild(apiKeyInput);
    inputsHolder.appendChild(secretKeyInput);

    // === BUTTONS ===
    const saveBtn = document.createElement("button");
    saveBtn.classList.add("submit-btn");
    saveBtn.textContent = "Gem nøgle";

    const cancelBtn = document.createElement("button");
    cancelBtn.classList.add("header-btn"); // reuse blue gradient
    cancelBtn.textContent = "Annuller";
    cancelBtn.style.marginTop = "1rem";

    // === Assemble ===
    container.appendChild(header);
    container.appendChild(info);
    container.appendChild(inputsHolder);
    container.appendChild(saveBtn);
    container.appendChild(cancelBtn);

    app.appendChild(container);

    // === EVENT HANDLERS ===
    saveBtn.addEventListener("click", async () => {
        const apiKey = apiKeyInput.value.trim();
        const secretKey = secretKeyInput.value.trim();

        if (!apiKey || !secretKey) {
            showPopupMessage("Begge felter skal udfyldes", "error");
            return;
        }

        try {
            const token = localStorage.getItem("token");
            const response = await fetch("http://localhost:8080/api/users/binance-key", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({ apiKey, secretKey })
            });

            if (!response.ok) throw new Error("Fejl ved oprettelse af API-nøgle");

            showPopupMessage("✅ API-nøgle gemt!", "success");
            await loadDashBoard();

        } catch (err) {
            console.error(err);
            showPopupMessage("Kunne ikke gemme API-nøgle", "error");
        }
    });

    cancelBtn.addEventListener("click", async () => {
        await loadLandingPage();
    });
}

