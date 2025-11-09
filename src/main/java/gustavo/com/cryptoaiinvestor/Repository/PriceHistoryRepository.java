package gustavo.com.cryptoaiinvestor.Repository;


import gustavo.com.cryptoaiinvestor.Models.Crypto;
import gustavo.com.cryptoaiinvestor.Models.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory,Integer> {
}
