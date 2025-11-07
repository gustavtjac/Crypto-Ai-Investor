package gustavo.com.cryptoaiinvestor.Service;

import gustavo.com.cryptoaiinvestor.Models.BinanceApiKey;

import gustavo.com.cryptoaiinvestor.Repository.BinanceApiKeyRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

@Service
public class BinanceApiKeysService {

    private final BinanceApiKeyRepository binanceApiKeyRepository;

    public BinanceApiKeysService(BinanceApiKeyRepository binanceApiKeyRepository) {
        this.binanceApiKeyRepository = binanceApiKeyRepository;
    }


    public boolean validateBinanceApikeys(BinanceApiKey binanceApiKey){
        try {
            String testnet = "https://testnet.binance.vision";
            String baseUrl = "https://api.binance.com";
            String endpoint = "/api/v3/account";
            long timestamp = System.currentTimeMillis();
            String query = "timestamp=" + timestamp;

            // Sign the request using HMAC SHA256
            String signature = sign(query, binanceApiKey.getPrivateKey());
            String url = testnet + endpoint + "?" + query + "&signature=" + signature;

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-MBX-APIKEY", binanceApiKey.getPublicKey());

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            if (e instanceof org.springframework.web.client.HttpClientErrorException httpError) {
                System.out.println("❌ Binance API Error: " + httpError.getResponseBodyAsString());
            } else {
                e.printStackTrace();
            }
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

}
