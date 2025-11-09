package gustavo.com.cryptoaiinvestor.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gustavo.com.cryptoaiinvestor.DTO.ActiveTrade;
import gustavo.com.cryptoaiinvestor.DTO.BinanceFrontEndInfo;
import gustavo.com.cryptoaiinvestor.DTO.OpenOrder;
import gustavo.com.cryptoaiinvestor.Models.*;
import gustavo.com.cryptoaiinvestor.Repository.CryptoRepository;
import gustavo.com.cryptoaiinvestor.Repository.PastTradesRepository;
import gustavo.com.cryptoaiinvestor.Repository.PriceHistoryRepository;
import gustavo.com.cryptoaiinvestor.Repository.TradingStrategyRepository;
import jakarta.persistence.GenerationType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BinanceLiveService {

    private final BinanceApiKeysService binanceApiKeysService;
    private final WebClient.Builder webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String BASE_URL = "https://testnet.binancefuture.com";
    private final GptService gptService;
    private final CryptoRepository cryptoRepository;
    private final PastTradesRepository pastTradesRepository;
    private final TradingStrategyRepository tradingStrategyRepository;

    public BinanceLiveService(BinanceApiKeysService binanceApiKeysService, WebClient.Builder webClient, GptService gptService, CryptoRepository cryptoRepository, PastTradesRepository pastTradesRepository, TradingStrategyRepository tradingStrategyRepository) {
        this.binanceApiKeysService = binanceApiKeysService;
        this.webClient = webClient;
        this.gptService = gptService;
        this.cryptoRepository = cryptoRepository;
        this.pastTradesRepository = pastTradesRepository;
        this.tradingStrategyRepository = tradingStrategyRepository;
    }

    public BinanceFrontEndInfo getAccountOverview(BinanceApiKey binanceApiKey) {
        BinanceApiKey decrypted = binanceApiKeysService.getDeCryptedKeys(binanceApiKey);
        BinanceFrontEndInfo info = new BinanceFrontEndInfo();

        Map<String, Double> balanceAndPnl = getBalance(decrypted);
        info.setBalance(balanceAndPnl.get("balance"));
        info.setUnrealizedProfit(balanceAndPnl.get("unrealizedProfit"));

        List<ActiveTrade> trades = getActiveTrades(decrypted);
        info.setActiveTrades(trades);

        List<OpenOrder> orders = getOpenOrders(decrypted);
        info.setOpenOrders(orders);

        return info;
    }

    // ✅ Reusable signed API call helper
    private JsonNode callSignedEndpoint(String endpoint, BinanceApiKey apiKey) throws Exception {
        long timestamp = System.currentTimeMillis();
        String query = "timestamp=" + timestamp;
        String signature = binanceApiKeysService.sign(query, apiKey.getPrivateKey());

        String response = webClient
                .baseUrl(BASE_URL)
                .defaultHeader("X-MBX-APIKEY", apiKey.getPublicKey())
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(endpoint)
                        .queryParam("timestamp", timestamp)
                        .queryParam("signature", signature)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(ex -> {
                    System.err.println("❌ Error calling " + endpoint + ": " + ex.getMessage());
                    return Mono.just("[]");
                })
                .block();

        return mapper.readTree(response);
    }

    // ✅ Uses callSignedEndpoint()
    private Map<String, Double> getBalance(BinanceApiKey binanceApiKey) {
        Map<String, Double> result = new HashMap<>();
        try {
            JsonNode jsonNode = callSignedEndpoint("/fapi/v2/balance", binanceApiKey);

            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    if ("USDT".equalsIgnoreCase(node.path("asset").asText())) {
                        double balance = node.path("balance").asDouble(0.0);
                        double unrealizedPnL = node.path("crossUnPnl").asDouble(0.0);
                        result.put("balance", balance);
                        result.put("unrealizedProfit", unrealizedPnL);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Exception in getBalance: " + e.getMessage());
        }

        result.putIfAbsent("balance", 0.0);
        result.putIfAbsent("unrealizedProfit", 0.0);
        return result;
    }

    // ✅ Uses callSignedEndpoint() for both positionRisk and openOrders
    private List<ActiveTrade> getActiveTrades(BinanceApiKey binanceApiKey) {
        List<ActiveTrade> trades = new ArrayList<>();

        try {
            JsonNode positionNodes = callSignedEndpoint("/fapi/v2/positionRisk", binanceApiKey);
            JsonNode ordersNodes = callSignedEndpoint("/fapi/v1/openOrders", binanceApiKey);

            for (JsonNode pos : positionNodes) {
                double posAmt = pos.path("positionAmt").asDouble(0.0);
                if (posAmt == 0.0) continue;

                ActiveTrade trade = new ActiveTrade();
                trade.setSymbol(pos.path("symbol").asText());
                trade.setPositionAmt(posAmt);
                trade.setEntryPrice(pos.path("entryPrice").asDouble(0.0));
                trade.setUnrealizedProfit(pos.path("unRealizedProfit").asDouble(0.0));
                trade.setLeverage(pos.path("leverage").asInt());
                trade.setPositionSide(posAmt > 0 ? "BUY" : "SELL");

                // Match SL/TP
                for (JsonNode order : ordersNodes) {
                    if (!order.path("symbol").asText().equals(trade.getSymbol())) continue;

                    String type = order.path("type").asText();
                    double price = order.path("stopPrice").asDouble(0.0);

                    if ("STOP_MARKET".equals(type) || "STOP".equals(type)) {
                        trade.setStopLoss(price);
                    } else if ("TAKE_PROFIT_MARKET".equals(type) || "TAKE_PROFIT".equals(type)) {
                        trade.setTakeProfit(price);
                    }
                }

                trades.add(trade);
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching active trades: " + e.getMessage());
        }

        return trades;
    }

    // ✅ Uses callSignedEndpoint()
    private List<OpenOrder> getOpenOrders(BinanceApiKey binanceApiKey) {
        List<OpenOrder> orders = new ArrayList<>();

        try {
            JsonNode jsonNode = callSignedEndpoint("/fapi/v1/openOrders", binanceApiKey);

            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    OpenOrder order = new OpenOrder();
                    order.setSymbol(node.path("symbol").asText());
                    order.setOrderId(node.path("orderId").asText());
                    order.setSide(node.path("side").asText());
                    order.setType(node.path("type").asText());
                    order.setPrice(node.path("price").asDouble());
                    order.setOrigQty(node.path("origQty").asDouble());
                    order.setExecutedQty(node.path("executedQty").asDouble());
                    order.setStatus(node.path("status").asText());
                    orders.add(order);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Exception in getOpenOrders: " + e.getMessage());
        }

        return orders;
    }



    public Object createNewTradeUsingGpt(User user) {
        try {
            System.out.println("test 1");
            BinanceApiKey decrypted = binanceApiKeysService.getDeCryptedKeys(user.getBinanceApiKey());
            JsonNode jsonNode = gptService.getTradeFromGpt();

            if (jsonNode == null) {
                throw new RuntimeException("GPT returned null response");
            }

            JsonNode binanceTradeNode = jsonNode.path("binanceTrade");
            JsonNode pastTradeNode = jsonNode.path("pastTrade");

            String symbol = binanceTradeNode.path("symbol").asText();
            String side = binanceTradeNode.path("side").asText();
            double quantity = binanceTradeNode.path("quantity").asDouble();
            int leverage = binanceTradeNode.path("leverage").asInt();
            double stopLoss = binanceTradeNode.path("stopLoss").asDouble();
            double takeProfit = binanceTradeNode.path("takeProfit").asDouble();

            // --- Build Binance client ---
            WebClient client = WebClient.builder()
                    .baseUrl(BASE_URL)
                    .defaultHeader("X-MBX-APIKEY", decrypted.getPublicKey())
                    .build();

            // --- 1. Set leverage ---
            long timestamp = System.currentTimeMillis();
            String leverageQuery = "symbol=" + symbol + "&leverage=" + leverage + "&timestamp=" + timestamp;
            String leverageSig = binanceApiKeysService.sign(leverageQuery, decrypted.getPrivateKey());

            client.post()
                    .uri(uriBuilder -> uriBuilder.path("/fapi/v1/leverage").query(leverageQuery + "&signature=" + leverageSig).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // --- 2. Place the main Market Order ---
            timestamp = System.currentTimeMillis();
            String orderQuery = String.format(
                    "symbol=%s&side=%s&type=MARKET&quantity=%s&timestamp=%d",
                    symbol, side, quantity, timestamp
            );
            String orderSig = binanceApiKeysService.sign(orderQuery, decrypted.getPrivateKey());

            String response = client.post()
                    .uri(uriBuilder -> uriBuilder.path("/fapi/v1/order").query(orderQuery + "&signature=" + orderSig).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(ex -> {
                        System.err.println("❌ Market order failed: " + ex.getMessage());
                        return Mono.just("{}");
                    })
                    .block();

            if (response == null || response.isEmpty() || response.equals("{}")) {
                throw new RuntimeException("Market order failed — no valid response from Binance.");
            }

            System.out.println("✅ Market Order Executed: " + response);

            // --- 3. Place Stop Loss and Take Profit orders ---
            String oppositeSide = side.equalsIgnoreCase("BUY") ? "SELL" : "BUY";

            // Stop Loss
            timestamp = System.currentTimeMillis();
            String stopLossQuery = String.format(
                    "symbol=%s&side=%s&type=STOP_MARKET&stopPrice=%s&closePosition=true&timestamp=%d",
                    symbol, oppositeSide, stopLoss, timestamp
            );
            String stopLossSig = binanceApiKeysService.sign(stopLossQuery, decrypted.getPrivateKey());

            String stopResponse = client.post()
                    .uri(uriBuilder -> uriBuilder.path("/fapi/v1/order").query(stopLossQuery + "&signature=" + stopLossSig).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("✅ Stop Loss order placed at " + stopLoss);

            // ✅ Take Profit
            timestamp = System.currentTimeMillis();

            String takeProfitQuery = String.format(
                    Locale.US,
                    "symbol=%s&side=%s&type=TAKE_PROFIT_MARKET&stopPrice=%.2f&closePosition=true&workingType=CONTRACT_PRICE&timestamp=%d",
                    symbol, oppositeSide, takeProfit, timestamp
            );

// ✅ Sign the raw query
            String takeProfitSig = binanceApiKeysService.sign(takeProfitQuery, decrypted.getPrivateKey());

// ✅ Build full URL manually — DO NOT use UriBuilder
            String finalTakeProfitUrl = "https://testnet.binancefuture.com/fapi/v1/order?" +
                    takeProfitQuery + "&signature=" + takeProfitSig;

// ✅ Debug output
            System.out.println("🔍 Final TP URL: " + finalTakeProfitUrl);

            String tpResponse = client.post()
                    .uri(finalTakeProfitUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> {
                                System.err.println("❌ Binance TP error: " + body);
                                return Mono.error(new RuntimeException("❌ Take Profit failed: " + body));
                            }))
                    .bodyToMono(String.class)
                    .block();

            System.out.println("✅ Take Profit order placed at " + takeProfit + " → " + tpResponse);



            // --- 4. If all successful, save PastTrade ---
            int cryptoId = pastTradeNode.path("cryptoId").asInt();
            int strategyId = pastTradeNode.path("tradingStrategyId").asInt();
            double entryPrice = pastTradeNode.path("entryPrice").asDouble();
            String profitAndStopLoss = pastTradeNode.path("profitAndStopLoss").asText();
            String positionType = pastTradeNode.path("positionType").asText();

            Crypto crypto = cryptoRepository.findById(cryptoId)
                    .orElseThrow(() -> new RuntimeException("Crypto not found: " + cryptoId));
            TradingStrategy strategy = tradingStrategyRepository.findById(strategyId)
                    .orElseThrow(() -> new RuntimeException("Trading strategy not found: " + strategyId));

            PastTrades pastTrade = new PastTrades();
            pastTrade.setCrypto(crypto);
            pastTrade.setTradingStrategy(strategy);
            pastTrade.setUser(user);
            pastTrade.setEntryPrice(entryPrice);
            pastTrade.setEntryTime(LocalDateTime.now());
            pastTrade.setProfitAndStopLoss(profitAndStopLoss);
            pastTrade.setPositionType(positionType);

            pastTradesRepository.save(pastTrade);
            System.out.println("💾 Saved PastTrade to database for " + symbol);

            return mapper.readTree(response);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Trade creation failed: " + e.getMessage());
        }
    }

    // helper for consistent price precision
    private String formatPrice(double price) {
        return String.format(Locale.US, "%.8f", price);
    }

}



