package gustavo.com.cryptoaiinvestor.Config;

import gustavo.com.cryptoaiinvestor.Models.User;
import gustavo.com.cryptoaiinvestor.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class init implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;


    @Override
    public void run(String... args) throws Exception {




        if (userRepository.findByUsername("admin").isEmpty()) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(encoder.encode("123"));
            user.setRole("ROLE_ADMIN");
            userRepository.save(user);
        }
    };
}

