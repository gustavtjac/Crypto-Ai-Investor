import { authorizedFetch, isTokenExpired } from "./securityMethods.js";
import { loadLandingPage } from "./landingpage.js";

const app = document.getElementById("app");

export async function loadDashBoard() {
    app.innerHTML = "";
    if (isTokenExpired()) {
        return await loadLandingPage("Din token er udløbet");
    }

    // ✅ Define mark price websockets upfront
    const markWs = {};
    const positions = {};

    // Connect to Binance WebSocket via backend
    await connectToBinanceWebSocket();

    // 🧱 Dashboard container
    const dashboardContainer = document.createElement("div");

    // 🔹 Header
    const dashBoardHeaderDiv = document.createElement("div");
    const backBtnImage = document.createElement("img");
    backBtnImage.src = "pictures/backbutton.png";
    backBtnImage.addEventListener("click", async function () {
        return loadLandingPage();
    });
    dashBoardHeaderDiv.appendChild(backBtnImage);
    dashboardContainer.appendChild(dashBoardHeaderDiv);

    // 🔹 Table for ongoing trades
    const table = document.createElement("table");
    table.classList.add("trade-table");

    const thead = document.createElement("thead");
    thead.innerHTML = `
    <tr>
      <th>Symbol</th>
      <th>Side</th>
      <th>Position</th>
      <th>Leverage</th>
      <th>Entry Price</th>
      <th>Mark Price</th>
      <th>Unrealized PnL (USDT)</th>
    </tr>
  `;
    table.appendChild(thead);

    const tbody = document.createElement("tbody");
    table.appendChild(tbody);
    dashboardContainer.appendChild(table);
    app.appendChild(dashboardContainer);

    // ⭐ Load initial positions
    await loadPositions();

    // ♻️ Poll positions every 5 seconds
    setInterval(loadPositions, 5000);

    // 🔹 Fetch and render positions
    async function loadPositions() {
        try {
            const response = await authorizedFetch("http://localhost:8080/api/binance/positions");
            if (!response.ok) throw new Error(await response.text());

            const positionsData = await response.json();
            const activePositions = positionsData.filter(p => parseFloat(p.positionAmt) !== 0);

            // Update the local positions object
            activePositions.forEach(p => {
                const amount = parseFloat(p.positionAmt);
                positions[p.symbol] = {
                    symbol: p.symbol,
                    positionAmt: amount,
                    entryPrice: parseFloat(p.entryPrice),
                    side: amount > 0 ? "BUY" : "SELL",
                    leverage: parseInt(p.leverage),
                    unrealizedPnL: parseFloat(p.unRealizedProfit),
                };
            });

            // Remove closed positions
            Object.keys(positions).forEach(symbol => {
                if (!activePositions.find(p => p.symbol === symbol)) {
                    delete positions[symbol];
                }
            });

            renderPositions();
        } catch (err) {
            console.error("❌ Failed to load positions:", err);
        }
    }

    // 🔹 Render table dynamically
    function renderPositions() {
        tbody.innerHTML = "";

        Object.values(positions).forEach((pos) => {
            const row = document.createElement("tr");
            row.innerHTML = `
        <td>${pos.symbol}</td>
        <td>${pos.side}</td>
        <td>${pos.positionAmt}</td>
        <td>${pos.leverage ?? "-"}</td>
        <td>${pos.entryPrice.toFixed(2)}</td>
        <td id="mark-${pos.symbol}">–</td>
        <td id="pnl-${pos.symbol}">${pos.unrealizedPnL.toFixed(2)}</td>
      `;
            tbody.appendChild(row);

            // Start price listener
            listenToMarkPrice(pos.symbol, pos.entryPrice, pos.positionAmt);
        });
    }

    // 🔹 Live mark price WS per symbol
    function listenToMarkPrice(symbol, entryPrice, positionAmt) {
        if (markWs[symbol]) return; // already listening
        const lower = symbol.toLowerCase();
        const ws = new WebSocket(`wss://stream.binancefuture.com/ws/${lower}@markPrice@1s`);

        ws.onmessage = (e) => {
            const d = JSON.parse(e.data);
            const markPrice = parseFloat(d.p);
            const pnl = (markPrice - entryPrice) * positionAmt;
            const markCell = document.getElementById(`mark-${symbol}`);
            const pnlCell = document.getElementById(`pnl-${symbol}`);
            if (markCell && pnlCell) {
                markCell.textContent = markPrice.toFixed(2);
                pnlCell.textContent = pnl.toFixed(4);
                pnlCell.style.color = pnl >= 0 ? "limegreen" : "red";
            }
        };

        ws.onclose = () => console.warn(`⚠️ Mark price WS closed for ${symbol}`);
        ws.onerror = (err) => console.error(`❌ Mark price WS error for ${symbol}:`, err);

        markWs[symbol] = ws;
    }
}

// ✅ Connect to Binance user data stream
async function connectToBinanceWebSocket() {
    try {
        const connectRes = await authorizedFetch("http://localhost:8080/api/binance/websocket/connect", {
            method: "POST",
        });

        if (!connectRes.ok) {
            return await loadLandingPage("Kunne ikke få adgang til Binance API: " + await connectRes.text());
        }

        console.log("✅ Binance WebSocket connection established on backend");
    } catch (err) {
        console.error("❌ Unexpected error connecting to Binance:", err);
        alert("❌ Unexpected error: " + err.message);
    }
}
