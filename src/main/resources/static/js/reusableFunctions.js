

export function showPopupMessage(popUpMsg) {
    // Remove any old popup
    const oldPopup = document.querySelector(".popup-message");
    if (oldPopup) oldPopup.remove();

    // Create popup container
    const popup = document.createElement("div");
    popup.classList.add("popup-message");
    popup.textContent = popUpMsg;

    // Add to page
    document.body.appendChild(popup);

    // Automatically remove after 3 seconds
    setTimeout(() => {
        popup.classList.add("fade-out");
        setTimeout(() => popup.remove(), 300);
    }, 3000);
}