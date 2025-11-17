import {loadLandingPage} from "./landingpage.js";


export function showPopupMessage(popUpMsg) {
    // Remove any old popup
    const oldPopup = document.querySelector(".popup-message");
    if (oldPopup) oldPopup.remove();

    // Create popup container
    const popup = document.createElement("div");
    popup.classList.add("popup-message");
    popup.textContent = popUpMsg;

    document.body.appendChild(popup);

    // Automatically remove after 3 seconds
    setTimeout(() => {
        popup.classList.add("fade-out");
        setTimeout(() => popup.remove(), 300);
    }, 6000);
}

export function showNewTrade(headerText, message) {
    // Play sound
    const sound = new Audio("/static/soundeffects/money.mp3");
    if (sound) sound.play().catch(() => {});

    // Create overlay
    const overlay = document.createElement("div");
    overlay.style.position = "fixed";
    overlay.style.top = "0";
    overlay.style.left = "0";
    overlay.style.width = "100vw";
    overlay.style.height = "100vh";
    overlay.style.background = "rgba(0,0,0,0.6)";
    overlay.style.display = "flex";
    overlay.style.alignItems = "center";
    overlay.style.justifyContent = "center";
    overlay.style.zIndex = "9999";
    overlay.style.backdropFilter = "blur(4px)";

    // Create popup box
    const popup = document.createElement("div");
    popup.style.background = "white";
    popup.style.borderRadius = "20px";
    popup.style.padding = "30px 40px";
    popup.style.maxWidth = "450px";
    popup.style.textAlign = "center";
    popup.style.boxShadow = "0 10px 25px rgba(0,0,0,0.3)";
    popup.style.animation = "popIn 0.5s ease";

    const header = document.createElement("h2");
    header.innerHTML = headerText;
    header.style.fontSize = "1.8rem";
    header.style.marginBottom = "15px";
    header.style.color = "#00b894";

    const body = document.createElement("p");
    body.innerHTML = message.replace(/\n/g, "<br>");
    body.style.fontSize = "1rem";
    body.style.color = "#333";

    const closeBtn = document.createElement("button");
    closeBtn.innerText = "Luk 🎯";
    closeBtn.style.marginTop = "20px";
    closeBtn.style.padding = "10px 20px";
    closeBtn.style.border = "none";
    closeBtn.style.borderRadius = "10px";
    closeBtn.style.background = "#00b894";
    closeBtn.style.color = "white";
    closeBtn.style.cursor = "pointer";
    closeBtn.onclick = () => document.body.removeChild(overlay);

    popup.appendChild(header);
    popup.appendChild(body);
    popup.appendChild(closeBtn);
    overlay.appendChild(popup);
    document.body.appendChild(overlay);

    // Confetti burst 🎉
    for (let i = 0; i < 5; i++) {
        setTimeout(() => {
            confetti({
                particleCount: 150,
                spread: 100,
                origin: { y: 0.6 },
            });
        }, i * 300);
    }



}

// 🎨 Simple CSS animation
const style = document.createElement('style');
style.innerHTML = `
@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}`;
document.head.appendChild(style);


export function createLogoutButton() {
    const btn = document.createElement("button");
    btn.id = "logoutBtn";
    btn.textContent = "Logout";
    btn.style.cursor = "pointer";

    btn.addEventListener("click", logout);
    return btn
}
async function logout() {

    localStorage.removeItem("token");

    return await loadLandingPage("Du er nu logget ud 🫨🫨")
}
