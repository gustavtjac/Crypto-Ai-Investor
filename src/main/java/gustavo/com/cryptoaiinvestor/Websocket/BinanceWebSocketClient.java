package gustavo.com.cryptoaiinvestor.Websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.Consumer;


public class BinanceWebSocketClient extends WebSocketClient {

    private final Consumer<String> messageHandler;


    public BinanceWebSocketClient(String listenKey, Consumer<String> messageHandler) {
        super(URI.create("wss://stream.binance.com:9443/ws/" + listenKey));
        this.messageHandler = messageHandler;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("✅ Connected to Binance WebSocket");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("📩 Message from Binance WS: " + message);
        messageHandler.accept(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("⚠️ Binance WebSocket closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("❌ Binance WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }
}
