package gustavo.com.cryptoaiinvestor.Repository;


import gustavo.com.cryptoaiinvestor.Models.PastTrades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PastTradesRepository extends JpaRepository<PastTrades,Integer> {
}
