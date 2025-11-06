package gustavo.com.cryptoaiinvestor.Models;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PastTrades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // --- Relationships ---
    @ManyToOne
    @JoinColumn(name = "crypto_id", nullable = false)
    private Crypto crypto;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "trading_strategy_id", nullable = false)
    private TradingStrategy tradingStrategy;

    // --- Trade details ---
    private Double entryPrice;

    private LocalDateTime entryTime;

    private String profitAndStopLoss;

    private String positionType; // e.g., "LONG" or "SHORT"

    public PastTrades() {
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPositionType() {
        return positionType;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    public String getProfitAndStopLoss() {
        return profitAndStopLoss;
    }

    public void setProfitAndStopLoss(String profitAndStopLoss) {
        this.profitAndStopLoss = profitAndStopLoss;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public Double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(Double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public TradingStrategy getTradingStrategy() {
        return tradingStrategy;
    }

    public void setTradingStrategy(TradingStrategy tradingStrategy) {
        this.tradingStrategy = tradingStrategy;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }
}
