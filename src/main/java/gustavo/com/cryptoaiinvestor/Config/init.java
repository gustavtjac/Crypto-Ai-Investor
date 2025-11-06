package gustavo.com.cryptoaiinvestor.Config;

import gustavo.com.cryptoaiinvestor.Models.Crypto;
import gustavo.com.cryptoaiinvestor.Models.User;
import gustavo.com.cryptoaiinvestor.Repository.CryptoRepository;
import gustavo.com.cryptoaiinvestor.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Component
public class init implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private CryptoRepository cryptoRepository;

    @Override
    public void run(String... args) throws Exception {




        List<Crypto> cryptos = Arrays.asList(
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Bitcoin", "btcusdt","https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1696501400"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Ethereum", "ethusdt","https://coin-images.coingecko.com/coins/images/279/large/ethereum.png?1696501628"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Ripple", "xrpusdt","https://coin-images.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1696501442"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "BNB", "bnbusdt","https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1696501970"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Solana", "solusdt","https://assets.coingecko.com/coins/images/4128/large/solana.png?1718769756"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "TRON", "trxusdt","https://assets.coingecko.com/coins/images/1094/large/tron-logo.png?1696502193"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Dogecoin", "dogeusdt","https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1696501409"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Cardano", "adausdt","https://assets.coingecko.com/coins/images/975/large/cardano.png?1696502090"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Avalanche", "avaxusdt","https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1696512369"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Chainlink", "linkusdt","https://assets.coingecko.com/coins/images/877/large/Chainlink_Logo_500.png?1760023405")
        );


 cryptos.forEach(c -> {
     if (!cryptoRepository.existsByTicker(c.getTicker())) {
         cryptoRepository.save(c);
     }
 });


        if (userRepository.findByUsername("admin").isEmpty()) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(encoder.encode("123"));
            user.setRole("ROLE_ADMIN");
            userRepository.save(user);
        }
    };
}

