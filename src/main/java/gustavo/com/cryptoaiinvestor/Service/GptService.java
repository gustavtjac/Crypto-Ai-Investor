    package gustavo.com.cryptoaiinvestor.Service;

    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import gustavo.com.cryptoaiinvestor.Models.Crypto;
    import gustavo.com.cryptoaiinvestor.Models.PriceHistory;
    import gustavo.com.cryptoaiinvestor.Models.TradingStrategy;
    import gustavo.com.cryptoaiinvestor.Repository.CryptoRepository;
    import gustavo.com.cryptoaiinvestor.Repository.TradingStrategyRepository;
    import org.springframework.stereotype.Service;

    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.web.reactive.function.client.WebClient;
    import reactor.core.publisher.Mono;

    import java.util.Comparator;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;
    import java.util.stream.Collectors;

    @Service
    public class GptService {

        private final WebClient webClient;
        private final ObjectMapper mapper = new ObjectMapper();
        private final CryptoRepository cryptoRepository;
        private final TradingStrategyRepository tradingStrategyRepository;

        @Value("${openai.api.key}")
        private String openAiApiKey;

        public GptService(WebClient.Builder webClientBuilder, CryptoRepository cryptoRepository, TradingStrategyRepository tradingStrategyRepository) {
            this.webClient = webClientBuilder.build();
            this.cryptoRepository = cryptoRepository;
            this.tradingStrategyRepository = tradingStrategyRepository;
        }

        public JsonNode getTradeFromGpt() {
            List<Crypto> cryptos = cryptoRepository.findAll();
            List<TradingStrategy> tradingStrategies = tradingStrategyRepository.findAll();

            // Map of crypto -> last 10 price points
            Map<Crypto, List<PriceHistory>> cryptoListMap = cryptos.stream()
                    .collect(Collectors.toMap(
                            crypto -> crypto,
                            crypto -> crypto.getPriceHistoryList().stream()
                                    .sorted(Comparator.comparing(PriceHistory::getTime).reversed())
                                    .limit(10)
                                    .collect(Collectors.toList())
                    ));

            try {
                // --- Format trading strategies ---
                StringBuilder strategyText = new StringBuilder();
                for (TradingStrategy strategy : tradingStrategies) {
                    strategyText.append("- ID: ").append(strategy.getId())
                            .append(", Name: ").append(strategy.getName())
                            .append(", Rules: ").append(strategy.getRules())
                            .append(", Description: ").append(strategy.getDescription() != null ? strategy.getDescription() : "N/A")
                            .append("\n");
                }

                // --- Format crypto and price history ---
                StringBuilder priceText = new StringBuilder();
                for (Map.Entry<Crypto, List<PriceHistory>> entry : cryptoListMap.entrySet()) {
                    Crypto crypto = entry.getKey();
                    priceText.append("Crypto ID: ").append(crypto.getId())
                            .append(", Ticker: ").append(crypto.getTicker())
                            .append(", PriceHistory: [");
                    entry.getValue().forEach(price ->
                            priceText.append(String.format(
                                    "{id: %d, time: %s, price: %.2f}, ",
                                    price.getId(), price.getTime(), price.getPrice()
                            ))
                    );
                    priceText.append("]\n");
                }

                // --- GPT Prompt ---
                String prompt = """
        You are a professional crypto trader using Binance Futures.
        You have access to the following internal data:

        Trading strategies (with IDs):
        %s

        Recent crypto price history (latest 10 entries per crypto, with IDs):
        %s

        Based on this data, generate One trade setup. Your trades are meant for short term gain 5-20 minute trades. The more confident you are the more leverage you should use. Dont be afraid to also do some  riskier trading
  
        Use your vast knowledge on the crypto market to make the most profitable trade.
        
        if prices are empty in list fint current prices on the internet
        
        dont be scared to use lesser known / secure coins if you see potential in them.

        Return ONLY valid JSON with two top-level objects:
        {
          "binanceTrade": {
            "symbol": " TICKERUSDT, (Use any of the Tickers from the list above, Use the one you have most confidence in) ",
            "side": "BUY" or "SELL",
            "entryPrice": (float),
            "leverage": (integer between 1 and 20),
            "quantity": (float, small lot size),
            "stopLoss": (float),
            "takeProfit": (float),
            "confidence": (float between 0.0 and 1.0)
          },
          "pastTrade": {
            "cryptoId": (Id of the crypto used for the trade),
            "tradingStrategyId": (Trading strategy used for the trade),
            "entryPrice": (Same as for binance trade),
            "profitAndStopLoss": "text summary of TP/SL from binanceTrade object",
            "positionType": "LONG" or "SHORT"
          }
        }

        Do NOT include explanations, markdown, or any extra text outside the JSON.
        """.formatted(strategyText, priceText);

                System.out.println(prompt);
                // --- Send to GPT ---
                String response = webClient
                        .post()
                        .uri("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + openAiApiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(Map.of(
                                "model", "gpt-4o",
                                "temperature", 0.4,
                                "messages", List.of(
                                        Map.of("role", "system", "content", "You are an expert crypto trader using Binance Futures."),
                                        Map.of("role", "user", "content", prompt)
                                )
                        ))
                        .retrieve()
                        .bodyToMono(String.class)
                        .onErrorResume(ex -> {
                            System.out.println("❌ GPT API error: " + ex.getMessage());
                            return Mono.just("{}");
                        })
                        .block();

                System.out.println("🧠 Raw GPT response:\n" + response);


                JsonNode root = mapper.readTree(response);


                String content = root.path("choices").get(0).path("message").path("content").asText();

                System.out.println("💬 GPT message content (raw):\n" + content);

                content = content.trim();
                if (content.startsWith("```")) {
                    content = content.replaceAll("(?s)```(json)?", "").trim();
                }

                int firstBrace = content.indexOf('{');
                int lastBrace = content.lastIndexOf('}');
                if (firstBrace != -1 && lastBrace != -1) {
                    content = content.substring(firstBrace, lastBrace + 1);
                }

                System.out.println("🧩 Cleaned GPT JSON:\n" + content);

                JsonNode result = mapper.readTree(content);

                if (!result.has("binanceTrade") || !result.has("pastTrade")) {
                    throw new RuntimeException("Ikke gyldigt format på gtp svar");
                }

                System.out.println("Færdigt gpt svar:\n" + result.toPrettyString());
                return result;

            } catch (Exception e) {
                System.err.println("❌ Noget git galt under genereringen af gpt traden" + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }



    }