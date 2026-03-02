package com.msp.configs;

import com.msp.enums.EUserRole;
import com.msp.models.User;
import com.msp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking the Initial Data...");
        createAdminUser();
        logCacheInfo();
        log.info("Data initialization Complete!");
    }

    private void createAdminUser() {
        String adminEmail = "mahingarodin@gmail.com";
        if(userRepository.findByEmail(adminEmail) == null) {
            log.info("Creating admin user...");
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin!123"))
                    .firstName("Mahinga")
                    .lastName("Rodin")
                    .role(EUserRole.ROLE_SUPER_ADMIN)
                    .phone("+250794415318")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            log.info("Admin user created!");
        } else {
            log.info("Admin user already exists!");
        }
    }

    private void logCacheInfo() {
        log.info("=== CACHE INFORMATION ===");
        log.info("Available cache names: users, products, orders, branches, categories, inventory, refunds, shifts");
        log.info("Cache TTL: 1 hour");
        log.info("To view cache stats: GET /api/admin/cache/stats");
        log.info("To clear cache: DELETE /api/admin/cache/clear/all");
        log.info("To check specific cache: GET /api/admin/cache/check/{cacheName}/{key}");
        log.info("=== END CACHE INFO ===");
    }
}
