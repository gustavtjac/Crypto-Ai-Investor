package gustavo.com.cryptoaiinvestor.Controller;


import gustavo.com.cryptoaiinvestor.Models.BinanceApiKey;
import gustavo.com.cryptoaiinvestor.Models.User;
import gustavo.com.cryptoaiinvestor.Service.BinanceApiKeysService;
import gustavo.com.cryptoaiinvestor.Service.BinanceLiveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.Authenticator;

@RestController
@RequestMapping("/api/binance")
public class BinanceApiController {


    private final BinanceLiveService liveService;
    private final BinanceApiKeysService binanceApiKeysService;

    public BinanceApiController(BinanceLiveService liveService, BinanceApiKeysService binanceApiKeysService) {
        this.liveService = liveService;
        this.binanceApiKeysService = binanceApiKeysService;
    }


    @PostMapping("/websocket/connect")
    public ResponseEntity<?> connect(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            BinanceApiKey keys = binanceApiKeysService.getDeCryptedKeys(user.getBinanceApiKey());

            liveService.connect(keys);
            return ResponseEntity.ok("✅ Connected to Binance User Stream.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Failed to connect to Binance: " + e.getMessage());
        }
    }

    @GetMapping("/websocket/stream")
    public SseEmitter stream() {
        return liveService.subscribe();
    }


}
