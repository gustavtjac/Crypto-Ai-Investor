import { authorizedFetch, isTokenExpired } from "./securityMethods.js";
import { loadLandingPage } from "./landingpage.js";
import {showPopupMessage} from "./reusableFunctions.js";

const app = document.getElementById("app");

export async function loadDashBoard() {
    app.innerHTML = "";

    if (isTokenExpired()) {
        return await loadLandingPage("Din token er udløbet");
    }

    const markWs = {};
    const positions = {};

    // --- Dashboard container ---
    const dashboardContainer = document.createElement("div");
    dashboardContainer.classList.add("dashboard-container");

    // --- Header ---
    const headerDiv = document.createElement("div");
    headerDiv.classList.add("dashboard-header");

    const backButton = document.createElement("img");
    backButton.src = "pictures/backbutton.png";
    backButton.alt = "Back";
    backButton.classList.add("back-button");
    backButton.addEventListener("click", async () => loadLandingPage());

    headerDiv.appendChild(backButton);
    dashboardContainer.appendChild(headerDiv);

    // --- Balance & PnL ---
    const balanceContainer = document.createElement("div");
    balanceContainer.classList.add("balance-container");

    const balanceLabel = document.createElement("p");
    balanceLabel.textContent = "Balance: ";

    const balanceValue = document.createElement("span");
    balanceValue.id = "balance-value";
    balanceValue.textContent = "–";
    balanceLabel.appendChild(balanceValue);
    balanceLabel.append(" USDT💰");

    const pnlLabel = document.createElement("p");
    pnlLabel.textContent = "Total PnL: ";

    const pnlValue = document.createElement("span");
    pnlValue.id = "total-pnl-value";
    pnlValue.textContent = "–";
    pnlLabel.appendChild(pnlValue);
    pnlLabel.append(" USDT💰");

    balanceContainer.appendChild(balanceLabel);
    balanceContainer.appendChild(pnlLabel);
    headerDiv.appendChild(balanceContainer)


    // --- Trades Table ---
    const table = document.createElement("table");
    table.classList.add("trade-table");

    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    const headers = [
        "Symbol",
        "Side",
        "Position (USDT)",
        "Leverage",
        "Entry Price",
        "Mark Price",
        "Unrealized PnL (USDT)",
        "Stop Loss",
        "Take Profit"
    ];

    headers.forEach(text => {
        const th = document.createElement("th");
        th.textContent = text;
        headerRow.appendChild(th);
    });

    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");
    table.appendChild(tbody);
    dashboardContainer.appendChild(table);
    app.appendChild(dashboardContainer);

    // --- Initial load ---
    await loadAccountInfo();
    setInterval(loadAccountInfo, 3000);

    const gptDiv = document.createElement("div")
    gptDiv.classList.add("gpt-div")


    const gptMakeTrade = document.createElement("btn")
    gptMakeTrade.classList.add("gpt-trade-btn")
    gptMakeTrade.textContent = "Tjen mig nogle penge hr. GPT 🤑🤖"
    gptMakeTrade.addEventListener("click", async function(){
        const svar = confirm("Er du sikker på du vil lade hr. GPT investere for dig :)")
        if (!svar){
           return showPopupMessage("Ingen investering blev fortaget")
        }

        const response = await authorizedFetch("http://localhost:8080/api/binance/newtrade", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (!response.ok){
            return showPopupMessage(response)
        }
        const data = await response.json()

        return showPopupMessage("Gpt har lavet følgende handel for dig" + data)

    })

gptDiv.appendChild(gptMakeTrade)
    dashboardContainer.appendChild(gptDiv)




    async function loadAccountInfo() {
        try {
            const response = await authorizedFetch("http://localhost:8080/api/binance/info");
            if (!response.ok) throw new Error(await response.text());

            const data = await response.json();
            const activeTrades = data.activeTrades || [];

            // Update balance from REST
            balanceValue.textContent = data.balance.toFixed(2);

            // Update local positions
            activeTrades.forEach(p => {
                const symbol = p.symbol;
                const amount = parseFloat(p.positionAmt);

                if (!positions[symbol]) {
                    positions[symbol] = {
                        symbol,
                        positionAmt: amount,
                        entryPrice: parseFloat(p.entryPrice),
                        side: amount > 0 ? "BUY" : "SELL",
                        leverage: parseInt(p.leverage),
                        unrealizedPnL: parseFloat(p.unrealizedProfit),
                        markPrice: null,
                        stopLoss: p.stopLoss ?? null,
                        takeProfit: p.takeProfit ?? null
                    };
                    createRow(positions[symbol]);
                    listenToMarkPrice(symbol);
                } else {
                    // Update only REST-based values
                    positions[symbol].unrealizedPnL = parseFloat(p.unrealizedProfit);
                    positions[symbol].stopLoss = p.stopLoss ?? null;
                    positions[symbol].takeProfit = p.takeProfit ?? null;
                    updatePnLCell(symbol);
                }
            });

            // Remove closed positions
            Object.keys(positions).forEach(symbol => {
                if (!activeTrades.find(p => p.symbol === symbol)) {
                    const row = document.getElementById(`row-${symbol}`);
                    if (row) row.remove();
                    delete positions[symbol];
                    if (markWs[symbol]) {
                        markWs[symbol].close();
                        delete markWs[symbol];
                    }
                }
            });

            // Update total account PnL (sum of all trade PnLs from REST)
            updateAccountPnL();
        } catch (err) {
            console.error("❌ Failed to load Binance account info:", err);
        }
    }

    function createRow(pos) {
        const row = document.createElement("tr");
        row.id = `row-${pos.symbol}`;

        const makeCell = (id, text) => {
            const td = document.createElement("td");
            if (id) td.id = id;
            td.textContent = text;
            return td;
        };

        const symbolTd = makeCell(null, pos.symbol);
        const sideTd = makeCell(null, pos.side);
        const posTd = makeCell(`position-${pos.symbol}`, "–");
        const levTd = makeCell(null, pos.leverage+"x" ?? "-");
        const entryTd = makeCell(null, pos.entryPrice.toFixed(2));
        const markTd = makeCell(`mark-${pos.symbol}`, "–");
        const pnlTd = makeCell(`pnl-${pos.symbol}`, pos.unrealizedPnL.toFixed(4));
        pnlTd.style.color = pos.unrealizedPnL >= 0 ? "limegreen" : "red";
        const slTd = makeCell(null, pos.stopLoss ? pos.stopLoss.toFixed(2) : "-");
        const tpTd = makeCell(null, pos.takeProfit ? pos.takeProfit.toFixed(2) : "-");

        [symbolTd, sideTd, posTd, levTd, entryTd, markTd, pnlTd, slTd, tpTd].forEach(td =>
            row.appendChild(td)
        );

        tbody.appendChild(row);
    }

    function updatePnLCell(symbol) {
        const pos = positions[symbol];
        const pnlCell = document.getElementById(`pnl-${symbol}`);
        if (!pnlCell || !pos) return;
        pnlCell.textContent = pos.unrealizedPnL.toFixed(4);
        pnlCell.style.color = pos.unrealizedPnL >= 0 ? "limegreen" : "red";
    }

    function updateAccountPnL() {
        const total = Object.values(positions).reduce(
            (sum, p) => sum + (p.unrealizedPnL || 0),
            0
        );
        pnlValue.textContent = total.toFixed(4);
        pnlValue.style.color = total >= 0 ? "limegreen" : "red";
    }

    function listenToMarkPrice(symbol) {
        if (markWs[symbol]) return;

        const lower = symbol.toLowerCase();
        const ws = new WebSocket(`wss://stream.binancefuture.com/ws/${lower}@markPrice@1s`);

        ws.onmessage = e => {
            const d = JSON.parse(e.data);
            const markPrice = parseFloat(d.p);
            const pos = positions[symbol];
            if (!pos) return;

            pos.markPrice = markPrice;

            const markCell = document.getElementById(`mark-${symbol}`);
            const posCell = document.getElementById(`position-${symbol}`);

            if (markCell) markCell.textContent = markPrice.toFixed(2);

            // Live update position value in USDT (no PnL math)
            if (posCell) {
                const notional = Math.abs(pos.positionAmt * markPrice);
                posCell.textContent = notional.toFixed(2);
            }
        };

        ws.onclose = () => console.warn(`⚠️ WS closed for ${symbol}`);
        ws.onerror = err => console.error(`❌ WS error for ${symbol}:`, err);

        markWs[symbol] = ws;
    }
}
