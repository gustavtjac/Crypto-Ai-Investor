package gustavo.com.cryptoaiinvestor.Models;


import jakarta.persistence.*;

import java.util.List;

@Entity
public class TradingStrategy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String rules;

    private String description;

    @OneToMany(mappedBy = "tradingStrategy")
    private List<PastTrades> pastTrades;

    public TradingStrategy(String name, String rules, String description, List<PastTrades> pastTrades) {
        this.name = name;
        this.rules = rules;
        this.description = description;
        this.pastTrades = pastTrades;
    }

    public List<PastTrades> getPastTrades() {
        return pastTrades;
    }

    public void setPastTrades(List<PastTrades> pastTrades) {
        this.pastTrades = pastTrades;
    }

    public TradingStrategy() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }
}
