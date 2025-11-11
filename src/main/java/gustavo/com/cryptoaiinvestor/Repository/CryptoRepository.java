package gustavo.com.cryptoaiinvestor.Repository;

import gustavo.com.cryptoaiinvestor.Models.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CryptoRepository extends JpaRepository<Crypto,Integer> {
    boolean existsByTicker(String ticker);
    Crypto findByTicker(String ticker);
}
