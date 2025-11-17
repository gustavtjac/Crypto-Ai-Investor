package gustavo.com.cryptoaiinvestor.Controller;

import gustavo.com.cryptoaiinvestor.DTO.LoginRequest;
import gustavo.com.cryptoaiinvestor.Models.User;
import gustavo.com.cryptoaiinvestor.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest){
        try {

return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginRequest));
        } catch (Exception e) {
            return  ResponseEntity.status(401).body("Forkert kode eller brugernavn");
        }

    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

      User user = (User) authentication.getPrincipal();

        Map<String, Object> response = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        );

        return ResponseEntity.ok(response);
    }

}
