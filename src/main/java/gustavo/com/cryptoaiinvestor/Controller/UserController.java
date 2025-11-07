package gustavo.com.cryptoaiinvestor.Controller;
import gustavo.com.cryptoaiinvestor.Models.BinanceApiKey;
import gustavo.com.cryptoaiinvestor.Models.User;
import gustavo.com.cryptoaiinvestor.Service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


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
            User savedUser = userService.inputPrivateKeys(binanceApiKey,user);

            if (savedUser.getBinanceApiKey() != null){
                return ResponseEntity.status(HttpStatus.OK).body("Binance Api-nøgle godkendt");
            }
            else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Binance Api-nøgle fejlede. Prøv igen");
            }
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
