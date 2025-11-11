package gustavo.com.cryptoaiinvestor.Service;


import gustavo.com.cryptoaiinvestor.Models.Crypto;
import gustavo.com.cryptoaiinvestor.Models.PriceHistory;
import gustavo.com.cryptoaiinvestor.Repository.CryptoRepository;
import gustavo.com.cryptoaiinvestor.Repository.PriceHistoryRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PriceHistoryService {

    private final CryptoRepository cryptoRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final WebClient webClient;

    public PriceHistoryService(CryptoRepository cryptoRepository, PriceHistoryRepository priceHistoryRepository) {
        this.cryptoRepository = cryptoRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.webClient = WebClient.create("https://testnet.binancefuture.com");
    }

    @Scheduled(cron = "0 0/2 * * * *", zone = "Europe/Copenhagen")
    public void saveCurrentPrices() {
        try {
            List<Crypto> cryptos = cryptoRepository.findAll();

            List<Map<String, Object>> allPrices = webClient.get()
                    .uri("/fapi/v1/ticker/price")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();

            if (allPrices == null) return;

            for (Crypto crypto : cryptos) {
                String symbol = crypto.getTicker().toUpperCase();

                allPrices.stream()
                        .filter(p -> symbol.equalsIgnoreCase((String) p.get("symbol")))
                        .findFirst()
                        .ifPresent(p -> {
                            double price = Double.parseDouble((String) p.get("price"));
                            PriceHistory ph = new PriceHistory();
                            ph.setCrypto(crypto);
                            ph.setPrice(price);
                            ph.setTime(LocalDateTime.now());
                            priceHistoryRepository.save(ph);
                        });
            }

            System.out.println("✅ Prices saved at " + LocalDateTime.now());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("⚠️ Failed to fetch prices from Binance: " + e.getMessage());
        }
    }
}