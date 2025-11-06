package gustavo.com.cryptoaiinvestor.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String username;

    @JsonIgnore
    private String password;
    private String binanceApiKey;
    private String role;

    @OneToMany(mappedBy = "user")
    private List<PastTrades> pastTrades;

    @Override
    public boolean isEnabled() {
        return true;
    }

    public List<PastTrades> getPastTrades() {
        return pastTrades;
    }

    public void setPastTrades(List<PastTrades> pastTrades) {
        this.pastTrades = pastTrades;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }
    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBinanceApiKey() {
        return binanceApiKey;
    }

    public void setBinanceApiKey(String binanceApiKey) {
        this.binanceApiKey = binanceApiKey;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
