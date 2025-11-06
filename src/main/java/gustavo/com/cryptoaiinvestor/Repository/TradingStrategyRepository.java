package gustavo.com.cryptoaiinvestor.Repository;


import gustavo.com.cryptoaiinvestor.Models.TradingStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradingStrategyRepository  extends JpaRepository<TradingStrategy,Integer> {
}
