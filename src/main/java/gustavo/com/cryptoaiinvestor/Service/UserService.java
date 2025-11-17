package gustavo.com.cryptoaiinvestor.Service;

import gustavo.com.cryptoaiinvestor.DTO.LoginRequest;
import gustavo.com.cryptoaiinvestor.Models.BinanceApiKey;
import gustavo.com.cryptoaiinvestor.Models.User;
import gustavo.com.cryptoaiinvestor.Repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

private final BinanceApiKeysService binanceApiKeysService;
private final UserRepository userRepository;
private final EncryptionService encryptionService;
private final PasswordEncoder passwordEncoder;



    public UserService(BinanceApiKeysService binanceApiKeysService, UserRepository userRepository, EncryptionService encryptionService, PasswordEncoder passwordEncoder) {
        this.binanceApiKeysService = binanceApiKeysService;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.passwordEncoder = passwordEncoder;
    }


    public User inputPrivateKeys(BinanceApiKey binanceApiKey, User user) {
        boolean valid = binanceApiKeysService.validateBinanceApikeys(binanceApiKey);
        if (!valid) {
            throw new RuntimeException("Ugyldige Binance API-nøgler");
        }

        String encryptedPrivate = encryptionService.encrypt(binanceApiKey.getPrivateKey());
        String encryptedPublic = encryptionService.encrypt(binanceApiKey.getPublicKey());

        binanceApiKey.setPrivateKey(encryptedPrivate);
        binanceApiKey.setPublicKey(encryptedPublic);
        binanceApiKey.setUser(user);
        user.setBinanceApiKey(binanceApiKey);
        return userRepository.save(user);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public String createNewUser(LoginRequest user) {
        User userToBeSaved = new User();
        userToBeSaved.setUsername(user.getUsername());
        if (userRepository.existsByUsername(user.getUsername())) {
           throw new RuntimeException("Username already exists, please try another");
        }
        userToBeSaved.setPassword(passwordEncoder.encode(user.getPassword()));

        userToBeSaved.setRole("ROLE_USER");
        userRepository.save(userToBeSaved);
        return "User Created, Please log in now";
    }
}
