import {authorizedFetch, isTokenExpired} from "./securityMethods.js";
import {loadLandingPage} from "./landingpage.js";


const app = document.getElementById("app")

export async function loadDashBoard(){

    app.innerHTML= "";
    if (isTokenExpired()){
        return await loadLandingPage("Din token er udløbet")
    }

    await connectToBinanceWebSocket()
//container der holder hele siden
    const dashboardContainer = document.createElement("div")
    //laver header
    const dashBoardHeaderDiv = document.createElement("div")
    const backBtnImage = document.createElement("img")
    backBtnImage.src = "pictures/backbutton.png"
    backBtnImage.addEventListener("click",async function(){
        return loadLandingPage();
    })
    dashBoardHeaderDiv.appendChild(backBtnImage)
    dashboardContainer.appendChild(dashBoardHeaderDiv)


    //se alle crypto priser


    app.appendChild(dashboardContainer)
}




async function connectToBinanceWebSocket() {
    try {

        const connectRes = await authorizedFetch("http://localhost:8080/api/binance/websocket/connect", {
            method: "POST",
        });

        if (!connectRes.ok) {

            return await loadLandingPage("Kunne ikke få adgang til binance api ", await connectRes.text())
        }



        const eventSource = new EventSource("http://localhost:8080/api/binance/websocket/stream");

        eventSource.onopen = () => {
            console.log("📡 SSE connection established with backend");
        };

        eventSource.onmessage = (event) => {
            console.log("📨 Binance message:", event.data);
            handleBinanceMessage(event.data);
        };

        eventSource.onerror = (error) => {
            console.error("⚠️ SSE connection error:", error);
            eventSource.close();
        };

    } catch (err) {
        console.error("❌ Unexpected error:", err);
        alert("❌ Unexpected error: " + err.message);
    }
}


function handleBinanceMessage(rawMessage) {
    try {
        const msg = JSON.parse(rawMessage);
        console.log("🔔 Parsed Binance event:", msg);

        // Example: you can handle specific event types here
        if (msg.e === "executionReport") {
            console.log("💰 Order update:", msg);
        } else if (msg.e === "outboundAccountPosition") {
            console.log("📊 Balance update:", msg);
        }
    } catch (e) {
        console.warn("⚠️ Non-JSON message:", rawMessage);
    }
}
