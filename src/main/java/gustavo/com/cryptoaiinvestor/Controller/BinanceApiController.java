package gustavo.com.cryptoaiinvestor.Controller;


import gustavo.com.cryptoaiinvestor.DTO.BinanceFrontEndInfo;
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

@RestController
@RequestMapping("/api/binance")
public class BinanceApiController {


    private final BinanceLiveService liveService;

    public BinanceApiController(BinanceLiveService liveService, BinanceApiKeysService binanceApiKeysService) {
        this.liveService = liveService;
    }

   @PostMapping("/newtrade")
   public ResponseEntity<?> createANewTradeUsingGpt(Authentication authentication){
       User user = (User) authentication.getPrincipal();
       return ResponseEntity.status(HttpStatus.OK).body(liveService.createNewTradeUsingGpt(user));
   }



    @GetMapping("/info")
    public ResponseEntity<BinanceFrontEndInfo> getAllInfo(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.OK).body(liveService.getAccountOverview(user.getBinanceApiKey()));

    }



}
