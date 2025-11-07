package gustavo.com.cryptoaiinvestor.Controller;
import gustavo.com.cryptoaiinvestor.Models.BinanceApiKey;
import gustavo.com.cryptoaiinvestor.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/binance-key")
    public ResponseEntity<?> inputPrivateKeys(@RequestBody BinanceApiKey binanceApiKey,Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        gustavo.com.cryptoaiinvestor.Models.User user =
                (gustavo.com.cryptoaiinvestor.Models.User) authentication.getPrincipal();

        boolean working = userService.inputPrivateKeys(binanceApiKey,user);


    }

    @GetMapping("/checkifapikey")
public ResponseEntity<?> checkIfUserHasApiKey(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        gustavo.com.cryptoaiinvestor.Models.User user =
                (gustavo.com.cryptoaiinvestor.Models.User) authentication.getPrincipal();
        return ResponseEntity.status(200).body(user.getBinanceApiKey() != null);
    }

}
