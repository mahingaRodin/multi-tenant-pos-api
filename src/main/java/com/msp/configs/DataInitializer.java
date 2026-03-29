package com.msp.configs;

import com.msp.enums.EStoreStatus;
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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void run(String... args) {
        log.info("=== Starting Data Initialization ===");

        try {
            // 1. Initialize or Fetch Admin
            String adminEmail = "mahingarodin@gmail.com";
            User adminUser = userRepository.findByEmail(adminEmail);

            if (adminUser == null) {
                log.info("Creating new admin user...");
                adminUser = User.builder()
                        // REMOVED: .id(UUID.randomUUID()) - Let Hibernate generate it
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
                adminUser = userRepository.save(adminUser);
            }

            // 2. Initialize or Fetch Store
            Store defaultStore;
            List<Store> existingStores = storeRepository.findAll();
            if (existingStores.isEmpty()) {
                log.info("Creating default store...");
                defaultStore = new Store();
                defaultStore.setBrand("SaaS POS Default");
                defaultStore.setStoreType("General Retail");
                defaultStore.setDescription("Automatic initialization store");
                defaultStore.setStoreAdmin(adminUser);
                defaultStore.setStatus(EStoreStatus.ACTIVE);
                defaultStore = storeRepository.save(defaultStore);
            } else {
                defaultStore = existingStores.get(0);
                if (defaultStore.getStoreAdmin() == null) {
                    defaultStore.setStoreAdmin(adminUser);
                }
            }

            // 3. Initialize or Fetch Branch
            Branch defaultBranch;
            List<Branch> existingBranches = branchRepository.findAll();
            if (existingBranches.isEmpty()) {
                log.info("Creating default branch...");
                defaultBranch = Branch.builder()
                        .name("Main Branch")
                        .address("Default Address")
                        .phone("+250794415312")
                        .email("umurungiolga12@gmail.com")
                        .store(defaultStore)
                        .workingDays(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"))
                        .openTime(LocalTime.of(8, 0))
                        .closeTime(LocalTime.of(20, 0))
                        .build();
                defaultBranch = branchRepository.save(defaultBranch);
            } else {
                defaultBranch = existingBranches.get(0);
                if (defaultBranch.getStore() == null) {
                    defaultBranch.setStore(defaultStore);
                }
            }

            // 4. Update Admin relationships if missing
            boolean adminNeedsUpdate = false;
            if (adminUser.getStore() == null) {
                adminUser.setStore(defaultStore);
                adminNeedsUpdate = true;
            }
            if (adminUser.getBranch() == null) {
                adminUser.setBranch(defaultBranch);
                adminNeedsUpdate = true;
            }

            if (adminNeedsUpdate) {
                adminUser.setUpdatedAt(LocalDateTime.now());
                // No need for saveAndFlush, the transaction commit will flush the dirty session state safely
                log.info("Admin updated successfully with store and branch");
            }

            log.info("=== Data Initialization Complete! ===");

        } catch (Exception e) {
            log.error("Error during data initialization", e);
            throw e;
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