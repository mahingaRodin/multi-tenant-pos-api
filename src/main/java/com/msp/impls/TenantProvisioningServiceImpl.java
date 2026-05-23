package com.msp.impls;

import com.msp.enums.ERegistrationStatus;
import com.msp.enums.EUserRole;
import com.msp.enums.EUserStatus;
import com.msp.exceptions.BusinessRegistrationException;
import com.msp.mappers.BusinessMapper;
import com.msp.models.Business;
import com.msp.models.TenantRegistration;
import com.msp.models.User;
import com.msp.payloads.dtos.BusinessDto;
import com.msp.payloads.response.ProvisioningResponse;
import com.msp.repositories.BusinessRepository;
import com.msp.repositories.TenantRegistrationRepository;
import com.msp.repositories.UserRepository;
import com.msp.services.TenantProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantProvisioningServiceImpl implements TenantProvisioningService {

    private final TenantRegistrationRepository registrationRepo;
    private final BusinessRepository businessRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ProvisioningResponse provisionTenant(UUID registrationId) {
        TenantRegistration reg = registrationRepo.findById(registrationId)
                .orElseThrow(() -> new BusinessRegistrationException(
                        "Registration not found: " + registrationId));

        if (reg.getStatus() != ERegistrationStatus.APPROVED) {
            throw new BusinessRegistrationException(
                    "Cannot provision a registration that is not APPROVED. Current status: " + reg.getStatus());
        }

        if (reg.getProvisionedTenantId() != null) {
            throw new BusinessRegistrationException(
                    "Tenant already provisioned for registration: " + registrationId);
        }

        // 1. Generate a stable tenantId
        UUID tenantId = UUID.randomUUID();

        // 2. Create the owner User with ROLE_STORE_ADMIN
        //    Temporary password = first 8 chars of tenantId (owner must change on first login)
        String tempPassword = tenantId.toString().replace("-", "").substring(0, 12);

        User owner = new User();
        owner.setFirstName(reg.getOwnerFirstName());
        owner.setLastName(reg.getOwnerLastName());
        owner.setEmail(reg.getOwnerEmail());
        owner.setPhone(reg.getOwnerPhone());
        owner.setRole(EUserRole.ROLE_STORE_ADMIN);
        owner.setPassword(passwordEncoder.encode(tempPassword));
        owner.setUserStatus(EUserStatus.ACTIVE);
        owner.setTenantId(tenantId);
        owner.setCreatedAt(LocalDateTime.now());
        owner.setUpdatedAt(LocalDateTime.now());
        owner.setLastLogin(null);
        owner = userRepo.save(owner);

        // 3. Create the Business entity
        Business business = Business.builder()
                .tenantId(tenantId)
                .businessName(reg.getBusinessName())
                .legalName(reg.getLegalName())
                .registrationNumber(reg.getRegistrationNumber())
                .country(reg.getCountry())
                .industry(reg.getIndustry())
                .description(reg.getBusinessDescription())
                .owner(owner)
                .build();
        business = businessRepo.save(business);

        // 4. Stamp the registration so we can never provision it twice
        reg.setProvisionedTenantId(tenantId);
        registrationRepo.save(reg);

        log.info("Tenant provisioned: tenantId={}, businessName={}, ownerEmail={}",
                tenantId, business.getBusinessName(), owner.getEmail());

        // TODO: publish TenantProvisionedEvent to SQS / send welcome email via SES
        // For now we log the temp password — in production this goes via email only
        log.info("Temporary credentials for {}: password={} (must be changed on first login)",
                owner.getEmail(), tempPassword);

        return new ProvisioningResponse(
                tenantId,
                business.getId(),
                owner.getEmail(),
                "Tenant provisioned successfully. Credentials sent to " + owner.getEmail()
        );
    }

    @Override
    public BusinessDto getTenantDetails(UUID tenantId) {
        Business business = businessRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new BusinessRegistrationException(
                        "No business found for tenantId: " + tenantId));
        return BusinessMapper.toDto(business);
    }
}
