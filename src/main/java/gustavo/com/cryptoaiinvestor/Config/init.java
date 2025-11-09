package gustavo.com.cryptoaiinvestor.Config;

import gustavo.com.cryptoaiinvestor.Models.Crypto;
import gustavo.com.cryptoaiinvestor.Models.PastTrades;
import gustavo.com.cryptoaiinvestor.Models.TradingStrategy;
import gustavo.com.cryptoaiinvestor.Models.User;
import gustavo.com.cryptoaiinvestor.Repository.CryptoRepository;
import gustavo.com.cryptoaiinvestor.Repository.TradingStrategyRepository;
import gustavo.com.cryptoaiinvestor.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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
    private PasswordEncoder encoder;

    @Autowired
    private CryptoRepository cryptoRepository;

    @Autowired
    private TradingStrategyRepository tradingStrategyRepository;

    @Value("${gustavo.encryption}")
    private String secret;

    @Override
    public void run(String... args) throws Exception {

        // --- Insert Cryptos ---
        List<Crypto> cryptos = Arrays.asList(
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Bitcoin", "btcusdt", "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1696501400"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Ethereum", "ethusdt", "https://coin-images.coingecko.com/coins/images/279/large/ethereum.png?1696501628"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Ripple", "xrpusdt", "https://coin-images.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1696501442"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "BNB", "bnbusdt", "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1696501970"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Solana", "solusdt", "https://assets.coingecko.com/coins/images/4128/large/solana.png?1718769756"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "TRON", "trxusdt", "https://assets.coingecko.com/coins/images/1094/large/tron-logo.png?1696502193"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Dogecoin", "dogeusdt", "https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1696501409"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Cardano", "adausdt", "https://assets.coingecko.com/coins/images/975/large/cardano.png?1696502090"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Avalanche", "avaxusdt", "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1696512369"),
                new Crypto(new ArrayList<>(), new ArrayList<>(), "Chainlink", "linkusdt", "https://assets.coingecko.com/coins/images/877/large/Chainlink_Logo_500.png?1760023405")
        );

        cryptos.forEach(c -> {
            if (!cryptoRepository.existsByTicker(c.getTicker())) {
                cryptoRepository.save(c);
            }
        });

        // --- Insert Admin User ---
        if (userRepository.findByUsername("admin").isEmpty()) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(encoder.encode("123"));
            user.setRole("ROLE_ADMIN");
            userRepository.save(user);
        }

        // --- Insert Trading Strategies ---
        if (tradingStrategyRepository.count() == 0) {
            List<TradingStrategy> strategies = Arrays.asList(
                    new TradingStrategy(
                            "Moving Average Crossover",
                            "Buy when short-term MA crosses above long-term MA; sell when it crosses below.",
                            "A trend-following strategy using moving average crossovers to identify market momentum.",
                            new ArrayList<>()
                    ),
                    new TradingStrategy(
                            "RSI Reversal",
                            "Buy when RSI < 30; sell when RSI > 70.",
                            "Exploits overbought and oversold conditions for reversal opportunities.",
                            new ArrayList<>()
                    ),
                    new TradingStrategy(
                            "Breakout Momentum",
                            "Enter a trade when price breaks above resistance or below support with high volume.",
                            "Captures high-momentum breakouts following consolidation.",
                            new ArrayList<>()
                    ),
                    new TradingStrategy(
                            "MACD Divergence",
                            "Buy when MACD line crosses above the signal line after bullish divergence.",
                            "Identifies early reversal signals using MACD indicator patterns.",
                            new ArrayList<>()
                    ),
                    new TradingStrategy(
                            "Bollinger Band Mean Reversion",
                            "Buy near lower Bollinger Band; sell near upper Bollinger Band.",
                            "Assumes prices revert to the mean after volatility expansion.",
                            new ArrayList<>()
                    ),
                    new TradingStrategy(
                            "Trendline Breakout",
                            "Buy when price closes above a downtrend line; sell when it closes below an uptrend line.",
                            "Uses simple price action to confirm breakout entries.",
                            new ArrayList<>()
                    ),
                    new TradingStrategy(
                            "Volume Spike Confirmation",
                            "Enter when breakout volume exceeds 2x average volume.",
                            "Filters false breakouts by confirming with strong volume.",
                            new ArrayList<>()
                    ),
                    new TradingStrategy(
                            "Fibonacci Retracement",
                            "Buy near 61.8%% retracement in uptrend; sell near 38.2%% retracement in downtrend.",
                            "Uses Fibonacci retracement levels for high-probability pullback entries.",
                            new ArrayList<>()
                    ),
                    new TradingStrategy(
                            "Moving Average Ribbon",
                            "Buy when all short-to-long moving averages align upward; sell when aligned downward.",
                            "Confirms strong multi-timeframe trends for continuation trades.",
                            new ArrayList<>()
                    ),
                    new TradingStrategy(
                            "Volatility Breakout",
                            "Enter when price breaks previous day’s high/low by a set percentage threshold.",
                            "Trades volatility expansion after consolidation phases.",
                            new ArrayList<>()
                    )
            );

            tradingStrategyRepository.saveAll(strategies);
            System.out.println("✅ Inserted 10 trading strategies.");
        } else {
            System.out.println("ℹ️ Trading strategies already exist — skipping initialization.");
        }
    }
}
