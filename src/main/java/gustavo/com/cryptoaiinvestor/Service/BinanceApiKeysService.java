package gustavo.com.cryptoaiinvestor.Service;

import gustavo.com.cryptoaiinvestor.Models.BinanceApiKey;

import gustavo.com.cryptoaiinvestor.Repository.BinanceApiKeyRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

@Service
public class BinanceApiKeysService {

    private final EncryptionService encryptionService;
    private final BinanceApiKeyRepository binanceApiKeyRepository;
    private final WebClient.Builder webClient;

    public BinanceApiKeysService(EncryptionService encryptionService, BinanceApiKeyRepository binanceApiKeyRepository, WebClient.Builder webClient) {
        this.encryptionService = encryptionService;
        this.binanceApiKeyRepository = binanceApiKeyRepository;
        this.webClient = webClient;
    }


    public boolean validateBinanceApikeys(BinanceApiKey binanceApiKey) {
        final String baseUrl = "https://api.binance.com";
        final String endpoint = "/api/v3/account";

        try {
            long timestamp = System.currentTimeMillis();
            String query = "timestamp=" + timestamp;
            String signature = sign(query, binanceApiKey.getPrivateKey());

            int status = webClient
                    .baseUrl(baseUrl)
                    .defaultHeader("X-MBX-APIKEY", binanceApiKey.getPublicKey())
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(endpoint)
                            .queryParam("timestamp", timestamp)
                            .queryParam("signature", signature)
                            .build())
                    .exchangeToMono(res -> Mono.just(res.statusCode().value()))
                    .onErrorResume(ex -> {
                        System.out.println("❌ Binance API Error: " + ex.getMessage());
                        return Mono.just(0);
                    })
                    .blockOptional()
                    .orElse(0);

            return status == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String sign(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }



    public BinanceApiKey save(BinanceApiKey binanceApiKey){
      return binanceApiKeyRepository.save(binanceApiKey);
    }

    public BinanceApiKey getDeCryptedKeys(BinanceApiKey binanceApiKey){
        BinanceApiKey decrypted = new BinanceApiKey();
        decrypted.setPublicKey(encryptionService.decrypt(binanceApiKey.getPublicKey()));
        decrypted.setPrivateKey(encryptionService.decrypt(binanceApiKey.getPrivateKey()));
        return decrypted;
    }

}
