package gustavo.com.cryptoaiinvestor.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gustavo.com.cryptoaiinvestor.Models.BinanceApiKey;
import gustavo.com.cryptoaiinvestor.Websocket.BinanceWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BinanceLiveService {
    private static final Logger log = LoggerFactory.getLogger(BinanceLiveService.class);

    private final WebClient.Builder webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private BinanceWebSocketClient socketClient;
    private BinanceApiKey currentKeys;
    private String listenKey;

    public BinanceLiveService(WebClient.Builder webClient) {
        this.webClient = webClient;
    }

    // 🔌 Connect to Binance (called from controller)
    public void connect(BinanceApiKey keys) {
        try {
            this.currentKeys = keys;
            this.listenKey = createListenKey(keys);
            openWebSocket(listenKey);
            log.info("✅ Connected to Binance user data stream");
        } catch (Exception e) {
            log.error("❌ Failed to connect to Binance: {}", e.getMessage(), e);
        }
    }

    // 🔊 Open WebSocket to Binance user data stream
    private void openWebSocket(String listenKey) {
        try {
            this.socketClient = new BinanceWebSocketClient(listenKey, this::broadcastMessage);
            this.socketClient.connect();
        } catch (Exception e) {
            log.error("❌ Failed to open Binance WebSocket: {}", e.getMessage(), e);
        }
    }

    // 📡 Broadcast message to all SSE subscribers
    private void broadcastMessage(String message) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (Exception e) {
                emitters.remove(emitter);
                log.warn("⚠️ Removed disconnected emitter: {}", e.getMessage());
            }
        }
    }

    // 👂 Subscribe to live events (SSE)
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    // 🗝️ Create Binance Listen Key via REST API
    private String createListenKey(BinanceApiKey keys) throws Exception {
        String response = webClient
                .baseUrl("https://api.binance.com")
                .defaultHeader("X-MBX-APIKEY", keys.getPublicKey())
                .build()
                .post()
                .uri("/api/v3/userDataStream")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        JsonNode json = mapper.readTree(response);
        String listenKey = json.get("listenKey").asText();
        return listenKey;
    }

    // 🔄 Keep the listenKey alive (Binance requirement)
    @Scheduled(fixedRate = 30 * 60 * 1000) // every 30 minutes
    public void keepAliveListenKey() {
        if (listenKey == null || currentKeys == null) return;

        try {
            webClient
                    .baseUrl("https://testnet.binance.vision")
                    .defaultHeader("X-MBX-APIKEY", currentKeys.getPublicKey())
                    .build()
                    .put()
                    .uri("/api/v3/userDataStream?listenKey=" + listenKey)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("🔄 Refreshed Binance listenKey");
        } catch (Exception e) {
            log.error("⚠️ Failed to refresh listenKey: {}", e.getMessage());
        }
    }

    // 🛑 Disconnect from Binance WebSocket
    public void disconnect() {
        try {
            if (socketClient != null) {
                socketClient.close();
                socketClient = null;
                log.info("🛑 Disconnected from Binance WebSocket");
            }
        } catch (Exception e) {
            log.error("⚠️ Error closing Binance WebSocket: {}", e.getMessage());
        } finally {
            listenKey = null;
            currentKeys = null;
        }
    }
}
