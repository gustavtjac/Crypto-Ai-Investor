package gustavo.com.cryptoaiinvestor.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gustavo.com.cryptoaiinvestor.DTO.ActiveTrade;
import gustavo.com.cryptoaiinvestor.DTO.BinanceFrontEndInfo;
import gustavo.com.cryptoaiinvestor.DTO.OpenOrder;
import gustavo.com.cryptoaiinvestor.Models.*;
import gustavo.com.cryptoaiinvestor.Repository.CryptoRepository;
import gustavo.com.cryptoaiinvestor.Repository.PastTradesRepository;
import gustavo.com.cryptoaiinvestor.Repository.TradingStrategyRepository;
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

    public BinanceLiveService(BinanceApiKeysService binanceApiKeysService,
                              WebClient.Builder webClient,
                              GptService gptService,
                              CryptoRepository cryptoRepository,
                              PastTradesRepository pastTradesRepository,
                              TradingStrategyRepository tradingStrategyRepository) {

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

        info.setActiveTrades(getActiveTrades(decrypted));
        info.setOpenOrders(getOpenOrders(decrypted));

        return info;
    }

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
                    System.err.println("Fejl ved kald til " + endpoint + ": " + ex.getMessage());
                    return Mono.just("[]");
                })
                .block();

        return mapper.readTree(response);
    }

    private Map<String, Double> getBalance(BinanceApiKey binanceApiKey) {
        Map<String, Double> result = new HashMap<>();
        try {
            JsonNode jsonNode = callSignedEndpoint("/fapi/v2/balance", binanceApiKey);

            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    if ("USDT".equalsIgnoreCase(node.path("asset").asText())) {
                        result.put("balance", node.path("crossWalletBalance").asDouble(0.0));
                        result.put("unrealizedProfit", node.path("crossUnPnl").asDouble(0.0));
                        break;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Fejl i getBalance: " + e.getMessage());
        }

        result.putIfAbsent("balance", 0.0);
        result.putIfAbsent("unrealizedProfit", 0.0);
        return result;
    }

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

                for (JsonNode order : ordersNodes) {
                    if (!order.path("symbol").asText().equals(trade.getSymbol())) continue;

                    String type = order.path("type").asText();
                    double price = order.path("stopPrice").asDouble(0.0);

                    if (type.contains("STOP")) {
                        trade.setStopLoss(price);
                    } else if (type.contains("TAKE_PROFIT")) {
                        trade.setTakeProfit(price);
                    }
                }

                trades.add(trade);
            }

        } catch (Exception e) {
            System.err.println("Fejl ved hentning af aktive handler: " + e.getMessage());
        }

        return trades;
    }

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
            System.err.println("Fejl i getOpenOrders: " + e.getMessage());
        }

        return orders;
    }

    public Object createNewTradeUsingGpt(User user) {
        try {
            BinanceApiKey decrypted = binanceApiKeysService.getDeCryptedKeys(user.getBinanceApiKey());
            JsonNode tradeFromGpt = gptService.getTradeFromGpt();

            if (tradeFromGpt == null) {
                throw new RuntimeException("GPT returnerede et ugyldigt svar");
            }

            JsonNode binanceTradeNode = tradeFromGpt.path("binanceTrade");
            JsonNode pastTradeNode = tradeFromGpt.path("pastTrade");

            String symbol = binanceTradeNode.path("symbol").asText();
            String side = binanceTradeNode.path("side").asText();
            double quantity = binanceTradeNode.path("quantity").asDouble();
            int leverage = binanceTradeNode.path("leverage").asInt();
            double stopLoss = binanceTradeNode.path("stopLoss").asDouble();
            double takeProfit = binanceTradeNode.path("takeProfit").asDouble();

            WebClient client = WebClient.builder()
                    .baseUrl(BASE_URL)
                    .defaultHeader("X-MBX-APIKEY", decrypted.getPublicKey())
                    .build();

            // Sæt gearing
            long timestamp = System.currentTimeMillis();
            String leverageQuery = "symbol=" + symbol + "&leverage=" + leverage + "&timestamp=" + timestamp;
            String leverageSig = binanceApiKeysService.sign(leverageQuery, decrypted.getPrivateKey());

            client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fapi/v1/leverage")
                            .query(leverageQuery + "&signature=" + leverageSig)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(ex -> Mono.error(new RuntimeException("Kunne ikke sætte leverage")))
                    .block();

            // Market order
            timestamp = System.currentTimeMillis();
            String orderQuery = String.format(
                    "symbol=%s&side=%s&type=MARKET&quantity=%s&timestamp=%d",
                    symbol, side, quantity, timestamp
            );
            String orderSig = binanceApiKeysService.sign(orderQuery, decrypted.getPrivateKey());

            client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fapi/v1/order")
                            .query(orderQuery + "&signature=" + orderSig)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(ex -> Mono.error(new RuntimeException("Market order mislykkedes")))
                    .block();

            // Stop loss
            String oppositeSide = side.equalsIgnoreCase("BUY") ? "SELL" : "BUY";
            timestamp = System.currentTimeMillis();

            String stopLossQuery = String.format(
                    "symbol=%s&side=%s&type=STOP_MARKET&stopPrice=%s&closePosition=true&timestamp=%d",
                    symbol, oppositeSide, stopLoss, timestamp
            );

            String stopLossSig = binanceApiKeysService.sign(stopLossQuery, decrypted.getPrivateKey());

            client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fapi/v1/order")
                            .query(stopLossQuery + "&signature=" + stopLossSig)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(ex -> Mono.error(new RuntimeException("Stop loss kunne ikke oprettes")))
                    .block();

            // Take profit
            timestamp = System.currentTimeMillis();

            String takeProfitQuery = String.format(
                    Locale.US,
                    "symbol=%s&side=%s&type=TAKE_PROFIT_MARKET&stopPrice=%.2f&closePosition=true&workingType=CONTRACT_PRICE&timestamp=%d",
                    symbol, oppositeSide, takeProfit, timestamp
            );

            String takeProfitSig = binanceApiKeysService.sign(takeProfitQuery, decrypted.getPrivateKey());

            client.post()
                    .uri("https://testnet.binancefuture.com/fapi/v1/order?" +
                            takeProfitQuery + "&signature=" + takeProfitSig)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class).flatMap(body ->
                                    Mono.error(new RuntimeException("Take profit kunne ikke oprettes"))
                            ))
                    .bodyToMono(String.class)
                    .block();

            // Gem tradehistorik
            int cryptoId = pastTradeNode.path("cryptoId").asInt();
            int strategyId = pastTradeNode.path("tradingStrategyId").asInt();

            Crypto crypto = cryptoRepository.findById(cryptoId)
                    .orElseThrow(() -> new RuntimeException("Crypto ikke fundet: " + cryptoId));

            TradingStrategy strategy = tradingStrategyRepository.findById(strategyId)
                    .orElseThrow(() -> new RuntimeException("Strategi ikke fundet: " + strategyId));

            PastTrades pastTrade = new PastTrades();
            pastTrade.setCrypto(crypto);
            pastTrade.setTradingStrategy(strategy);
            pastTrade.setUser(user);
            pastTrade.setEntryPrice(pastTradeNode.path("entryPrice").asDouble());
            pastTrade.setEntryTime(LocalDateTime.now());
            pastTrade.setProfitAndStopLoss(pastTradeNode.path("profitAndStopLoss").asText());
            pastTrade.setPositionType(pastTradeNode.path("positionType").asText());

            pastTradesRepository.save(pastTrade);

            return tradeFromGpt;

        } catch (Exception e) {
            throw new RuntimeException("Fejl under oprettelse af trade: " + e.getMessage());
        }
    }

    private String formatPrice(double price) {
        return String.format(Locale.US, "%.8f", price);
    }
}
