package gustavo.com.cryptoaiinvestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CryptoAiInvestorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoAiInvestorApplication.class, args);
    }

}
