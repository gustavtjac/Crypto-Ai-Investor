package gustavo.com.cryptoaiinvestor.Models;


import jakarta.persistence.*;

import java.util.List;

@Entity
public class Crypto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String ticker;


    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "crypto")
    private List<PriceHistory> priceHistoryList;

    @OneToMany(mappedBy ="crypto")
    private List<PastTrades> pastTrades;


    public List<PastTrades> getPastTrades() {
        return pastTrades;
    }

    public void setPastTrades(List<PastTrades> pastTrades) {
        this.pastTrades = pastTrades;
    }

    public Crypto() {
    }

    public List<PriceHistory> getPriceHistoryList() {
        return priceHistoryList;
    }

    public void setPriceHistoryList(List<PriceHistory> priceHistoryList) {
        this.priceHistoryList = priceHistoryList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
