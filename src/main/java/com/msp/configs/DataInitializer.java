package com.msp.configs;

import com.msp.enums.EUserRole;
import com.msp.models.Branch;
import com.msp.models.Store;
import com.msp.models.User;
import com.msp.repositories.BranchRepository;
import com.msp.repositories.StoreRepository;
import com.msp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking the Initial Data...");

        Store defaultStore = ensureDefaultStore();
        Branch defaultBranch = ensureDefaultBranch(defaultStore);

        createAdminUser(defaultStore, defaultBranch);
        logCacheInfo();
        log.info("Data initialization Complete!");
    }

    private Store ensureDefaultStore() {
        if (storeRepository.count() == 0) {
            log.info("Creating default store...");
            Store store = new Store();
            store.setBrand("SaaS POS Default");
            store.setStoreType("General Retail");
            store.setDescription("Automatic initialization store");
            return storeRepository.save(store);
        }
        return storeRepository.findAll().get(0);
    }

    private Branch ensureDefaultBranch(Store store) {
        if (branchRepository.count() == 0) {
            log.info("Creating default branch...");
            Branch branch = Branch.builder()
                    .name("Main Branch")
                    .address("Default Address")
                    .phone("+250794415312")
                    .email("umurungiolga12@gmail.com")
                    .store(store)
                    .workingDays(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"))
                    .openTime(LocalTime.of(8, 0))
                    .closeTime(LocalTime.of(20, 0))
                    .build();
            return branchRepository.save(branch);
        }
        return branchRepository.findAll().get(0);
    }

    private void createAdminUser(Store store, Branch branch) {
        String adminEmail = "mahingarodin@gmail.com";
        User existingUser = userRepository.findByEmail(adminEmail);

        if (existingUser == null) {
            log.info("Creating admin user...");
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin!123"))
                    .firstName("Mahinga")
                    .lastName("Rodin")
                    .role(EUserRole.ROLE_SUPER_ADMIN)
                    .phone("+250794415318")
                    .store(store)
                    .branch(branch)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .lastLogin(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            log.info("Admin user created and linked to store/branch!");
        } else {
            log.info("Updating existing admin user permissions...");
            boolean changed = false;

            if (existingUser.getRole() != EUserRole.ROLE_SUPER_ADMIN) {
                existingUser.setRole(EUserRole.ROLE_SUPER_ADMIN);
                changed = true;
            }

            if (existingUser.getStore() == null) {
                existingUser.setStore(store);
                changed = true;
            }

            if (existingUser.getBranch() == null) {
                existingUser.setBranch(branch);
                changed = true;
            }

            if (changed) {
                userRepository.save(existingUser);
                log.info("Admin user updated and synchronized.");
            } else {
                log.info("Admin user already synchronized.");
            }
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
