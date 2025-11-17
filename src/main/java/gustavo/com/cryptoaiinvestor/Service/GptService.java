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
import java.util.stream.Collectors;

@Service
public class GptService {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CryptoRepository cryptoRepository;
    private final TradingStrategyRepository tradingStrategyRepository;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public GptService(WebClient.Builder webClientBuilder,
                      CryptoRepository cryptoRepository,
                      TradingStrategyRepository tradingStrategyRepository) {

        this.webClient = webClientBuilder.build();
        this.cryptoRepository = cryptoRepository;
        this.tradingStrategyRepository = tradingStrategyRepository;
    }

    public JsonNode getTradeFromGpt() {
        List<Crypto> cryptos = cryptoRepository.findAll();
        List<TradingStrategy> tradingStrategies = tradingStrategyRepository.findAll();

        // Saml de seneste 10 pris-punkter for hver coin
        Map<Crypto, List<PriceHistory>> cryptoListMap = cryptos.stream()
                .collect(Collectors.toMap(
                        c -> c,
                        c -> c.getPriceHistoryList().stream()
                                .sorted(Comparator.comparing(PriceHistory::getTime).reversed())
                                .limit(10)
                                .collect(Collectors.toList())
                ));

        try {
            // Formater strategier til prompt
            StringBuilder strategyText = new StringBuilder();
            for (TradingStrategy strategy : tradingStrategies) {
                strategyText.append("- ID: ").append(strategy.getId())
                        .append(", Name: ").append(strategy.getName())
                        .append(", Rules: ").append(strategy.getRules())
                        .append(", Description: ")
                        .append(strategy.getDescription() != null ? strategy.getDescription() : "N/A")
                        .append("\n");
            }

            // Formater prisdata til prompt
            StringBuilder priceText = new StringBuilder();
            for (Map.Entry<Crypto, List<PriceHistory>> entry : cryptoListMap.entrySet()) {
                Crypto crypto = entry.getKey();
                priceText.append("Crypto ID: ").append(crypto.getId())
                        .append(", Ticker: ").append(crypto.getTicker())
                        .append(", PriceHistory: [");

                entry.getValue().forEach(p ->
                        priceText.append(String.format(
                                "{id: %d, time: %s, price: %.2f}, ",
                                p.getId(), p.getTime(), p.getPrice()
                        ))
                );

                priceText.append("]\n");
            }

            // GPT prompt – kort og uden floromvundet tekst
            String prompt = """
                    You are a Binance Futures trader using the following internal data:

                    Trading strategies:
                    %s

                    Price history (10 latest per crypto):
                    %s

                    Create ONE trade based on the data. 
                    Keep it short-term (5–20 min). 
                    Use JSON only, no extra text.
                    """.formatted(strategyText, priceText);

            // Kald OpenAI API
            String response = webClient
                    .post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of(
                            "model", "gpt-4o",
                            "temperature", 0.4,
                            "messages", List.of(
                                    Map.of("role", "system", "content", "You generate a single futures trade."),
                                    Map.of("role", "user", "content", prompt)
                            )
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(ex -> {
                        System.out.println("GPT API fejl: " + ex.getMessage());
                        return Mono.just("{}");
                    })
                    .block();

            JsonNode root = mapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // Rens GPT-output
            content = content.trim();
            if (content.startsWith("```")) {
                content = content.replaceAll("(?s)```(json)?", "").trim();
            }

            int firstBrace = content.indexOf('{');
            int lastBrace = content.lastIndexOf('}');
            if (firstBrace != -1 && lastBrace != -1) {
                content = content.substring(firstBrace, lastBrace + 1);
            }

            JsonNode result = mapper.readTree(content);

            if (!result.has("binanceTrade") || !result.has("pastTrade")) {
                throw new RuntimeException("GPT svaret mangler forventede felter");
            }

            return result;

        } catch (Exception e) {
            System.err.println("Fejl ved generering af trade: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
