package org.example.config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
/**
 * Cria dados de exemplo ao iniciar a aplicação.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .active(true)
                    .build();
            User user = User.builder()
                    .name("John Doe")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user1234"))
                    .role(Role.ROLE_USER)
                    .active(true)
                    .build();
            userRepository.save(admin);
            userRepository.save(user);
            log.info("=== Default users created ===");
            log.info("ADMIN -> admin@example.com / admin123");
            log.info("USER  -> user@example.com  / user1234");
            log.info("Swagger UI: http://localhost:8080/swagger-ui.html");
        }
    }
}
