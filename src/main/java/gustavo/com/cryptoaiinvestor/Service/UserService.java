package gustavo.com.cryptoaiinvestor.Service;

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



    public UserService(BinanceApiKeysService binanceApiKeysService, UserRepository userRepository, EncryptionService encryptionService) {
        this.binanceApiKeysService = binanceApiKeysService;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
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
}
