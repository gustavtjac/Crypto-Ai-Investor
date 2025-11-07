package gustavo.com.cryptoaiinvestor.Repository;


import gustavo.com.cryptoaiinvestor.Models.BinanceApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinanceApiKeyRepository extends JpaRepository<BinanceApiKey,Integer> {
}
