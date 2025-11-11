package gustavo.com.cryptoaiinvestor.Service;


import gustavo.com.cryptoaiinvestor.Config.JwtUtil;
import gustavo.com.cryptoaiinvestor.DTO.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }


    public Map<String,Object> login(LoginRequest loginRequest) {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword())
            );

        String token = jwtUtil.generateToken(loginRequest.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", loginRequest.getUsername());
        return response;
    }
}
