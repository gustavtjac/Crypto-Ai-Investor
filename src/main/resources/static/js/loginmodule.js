import {loadLandingPage} from "./landingpage.js";

export async function createLoginModule() {
    const oldOverlay = document.querySelector(".login-overlay");
    if (oldOverlay) oldOverlay.remove();

    let loginOrSignUp = true;

    // Overlayskærm der holder hele login-vinduet
    const overlay = document.createElement("div");
    overlay.classList.add("login-overlay", "hidden");

    const loginFormContainer = document.createElement("div");
    loginFormContainer.classList.add("login-container");

    const loginForm = document.createElement("div");
    loginForm.classList.add("login-form");

    const headerText = document.createElement("h1");
    headerText.classList.add("login-header");
    headerText.textContent = "Login";
    loginForm.appendChild(headerText);

    // Knapper til at skifte mellem Login / Register
    const buttonHolder = document.createElement("div");
    buttonHolder.classList.add("button-holder");

    const chooseLoginBtn = document.createElement("button");
    chooseLoginBtn.classList.add("login-btn", "active");
    chooseLoginBtn.textContent = "Login";
    chooseLoginBtn.addEventListener("click", function () {
        loginOrSignUp = true;
        submitBtn.textContent = "Login";
        headerText.textContent = "Login";
        chooseLoginBtn.classList.add("active");
        chooseRegisterButton.classList.remove("active");
    });

    const chooseRegisterButton = document.createElement("button");
    chooseRegisterButton.classList.add("register-btn");
    chooseRegisterButton.textContent = "Register";
    chooseRegisterButton.addEventListener("click", function () {
        loginOrSignUp = false;
        submitBtn.textContent = "Register";
        headerText.textContent = "Register";
        chooseRegisterButton.classList.add("active");
        chooseLoginBtn.classList.remove("active");
    });

    buttonHolder.appendChild(chooseLoginBtn);
    buttonHolder.appendChild(chooseRegisterButton);
    loginForm.appendChild(buttonHolder);

    // Inputs til brugernavn + kodeord
    const inputsHolder = document.createElement("div");
    inputsHolder.classList.add("inputs-holder");

    const message = document.createElement("h3");
    message.id = "message";
    message.classList.add("message", "hidden");
    inputsHolder.appendChild(message);

    const usernameInput = document.createElement("input");
    usernameInput.classList.add("input-field");
    usernameInput.placeholder = "username";
    usernameInput.type = "text";

    const passwordInput = document.createElement("input");
    passwordInput.classList.add("input-field");
    passwordInput.type = "password";
    passwordInput.placeholder = "password";

    inputsHolder.appendChild(usernameInput);
    inputsHolder.appendChild(passwordInput);

    loginForm.appendChild(inputsHolder);

    // Knap til at sende login / register
    const submitBtn = document.createElement("button");
    submitBtn.classList.add("submit-btn");
    submitBtn.textContent = "Login";

    submitBtn.addEventListener("click", async function () {
        const messageBox = document.getElementById("message");
        messageBox.textContent = "";

        try {
            const data = await authenticateUser(
                loginOrSignUp,
                usernameInput.value,
                passwordInput.value
            );

            // Hvis login
            if (loginOrSignUp === true){
                const succesMessage = "✅ Du er nu logget ind " + data.username + "!";
                messageBox.textContent = succesMessage;
                messageBox.classList.remove("error");
                messageBox.classList.add("success");

                localStorage.setItem("token", data.token);

                return await loadLandingPage(succesMessage);

                // Hvis register
            } else {
                const succesMessage = "✅ " + data + " 👽";
                messageBox.textContent = succesMessage;
                messageBox.classList.remove("error");
                messageBox.classList.add("success");

                return await loadLandingPage(succesMessage);
            }

        } catch (error) {
            messageBox.textContent = "❌ " + error.message;
            messageBox.classList.remove("success");
            messageBox.classList.add("error");
        }
    });

    loginForm.appendChild(submitBtn);
    loginFormContainer.appendChild(loginForm);
    overlay.appendChild(loginFormContainer);
    document.body.appendChild(overlay);

    // Åbn popup
    window.openLoginPopup = function() {
        overlay.classList.remove("hidden");
    };

    // Luk popup
    window.closeLoginPopup = function() {
        overlay.classList.add("hidden");
    };

    // Luk hvis man klikker udenfor
    overlay.addEventListener("click", (e) => {
        if (e.target === overlay) {
            closeLoginPopup();
        }
    });
}

async function authenticateUser(loginOrSignUp, username, password) {
    // Vælg rigtige endpoint afhængigt af login/register
    const url = loginOrSignUp
        ? "http://localhost:8080/api/auth/login"
        : "http://localhost:8080/api/users";

    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({ username, password })
        });

        // Hvis fejl fra backend
        if (!response.ok) {
            const errorMessage = await response.text();
            throw new Error(errorMessage);
        }

        // Ved login → backend sender JSON
        if (loginOrSignUp) {
            return await response.json();
        }

        // Ved register → backend sender tekst
        return await response.text();

    } catch (error) {
        console.error("Authentication failed:", error);
        throw error;
    }
}
