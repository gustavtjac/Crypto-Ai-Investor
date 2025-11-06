package gustavo.com.cryptoaiinvestor.Repository;


import gustavo.com.cryptoaiinvestor.Models.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory,Integer> {
}
