package gustavo.com.cryptoaiinvestor.Models;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "crypto_id", nullable = false)
    private Crypto crypto;

    @Column(nullable = false)
    private LocalDateTime time;
    @Column(nullable = false)
    private Double price;


    public PriceHistory() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }
}
