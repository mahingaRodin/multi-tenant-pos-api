package com.msp.impls;

import com.msp.enums.ERegistrationStatus;
import com.msp.exceptions.BusinessRegistrationException;
import com.msp.mappers.TenantRegistrationMapper;
import com.msp.models.TenantRegistration;
import com.msp.models.User;
import com.msp.payloads.dtos.TenantRegistrationDto;
import com.msp.payloads.request.BusinessRegistrationRequest;
import com.msp.payloads.response.ProvisioningResponse;
import com.msp.repositories.TenantRegistrationRepository;
import com.msp.repositories.UserRepository;
import com.msp.services.BusinessRegistrationService;
import com.msp.services.TenantProvisioningService;
import com.msp.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessRegistrationServiceImpl implements BusinessRegistrationService {

    private final TenantRegistrationRepository registrationRepo;
    private final UserRepository userRepo;
    private final TenantProvisioningService provisioningService;

    // ── Submit ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TenantRegistrationDto submitRegistration(BusinessRegistrationRequest request) {

        // Uniqueness checks
        if (registrationRepo.existsByOwnerEmail(request.getOwnerEmail())) {
            throw new BusinessRegistrationException(
                    "A registration with email '" + request.getOwnerEmail() + "' already exists.");
        }
        if (userRepo.findByEmail(request.getOwnerEmail()) != null) {
            throw new BusinessRegistrationException(
                    "An account with email '" + request.getOwnerEmail() + "' already exists.");
        }
        if (registrationRepo.existsByBusinessName(request.getBusinessName())) {
            throw new BusinessRegistrationException(
                    "A registration with business name '" + request.getBusinessName() + "' already exists.");
        }

        TenantRegistration reg = TenantRegistration.builder()
                .ownerFirstName(request.getOwnerFirstName())
                .ownerLastName(request.getOwnerLastName())
                .ownerEmail(request.getOwnerEmail())
                .ownerPhone(request.getOwnerPhone())
                .businessName(request.getBusinessName())
                .legalName(request.getLegalName())
                .registrationNumber(request.getRegistrationNumber())
                .country(request.getCountry())
                .industry(request.getIndustry())
                .businessDescription(request.getBusinessDescription())
                .status(ERegistrationStatus.PENDING)
                .build();

        reg = registrationRepo.save(reg);

        log.info("Business registration submitted: id={}, businessName={}, ownerEmail={}",
                reg.getId(), reg.getBusinessName(), reg.getOwnerEmail());

        // TODO: publish RegistrationSubmittedEvent to SQS → SES confirmation email
        // For now we log it
        log.info("TODO: send confirmation email to {}", reg.getOwnerEmail());

        return TenantRegistrationMapper.toDto(reg);
    }

    // ── Read ────────────────────────────────────────────────────────────────

    @Override
    public TenantRegistrationDto getRegistration(UUID registrationId) {
        TenantRegistration reg = findOrThrow(registrationId);
        return TenantRegistrationMapper.toDto(reg);
    }

    @Override
    public Page<TenantRegistrationDto> listRegistrations(ERegistrationStatus status, Pageable pageable) {
        if (status != null) {
            return registrationRepo.findByStatus(status, pageable)
                    .map(TenantRegistrationMapper::toDto);
        }
        return registrationRepo.findAll(pageable)
                .map(TenantRegistrationMapper::toDto);
    }

    // ── Admin decisions ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProvisioningResponse approveRegistration(UUID registrationId, String adminNotes) {
        TenantRegistration reg = findOrThrow(registrationId);
        assertTransitionAllowed(reg, ERegistrationStatus.APPROVED);

        // Resolve the current admin from the security context
        User admin = getCurrentAdminFromContext();

        reg.setStatus(ERegistrationStatus.APPROVED);
        reg.setAdminNotes(adminNotes);
        reg.setReviewedBy(admin);
        reg.setReviewedAt(LocalDateTime.now());
        registrationRepo.save(reg);

        log.info("Registration approved: id={}, by={}", registrationId,
                admin != null ? admin.getEmail() : "system");

        // Provision the tenant — creates Business + owner User in one transaction
        return provisioningService.provisionTenant(registrationId);
    }

    @Override
    @Transactional
    public TenantRegistrationDto rejectRegistration(UUID registrationId, String rejectionReason) {
        TenantRegistration reg = findOrThrow(registrationId);
        assertTransitionAllowed(reg, ERegistrationStatus.REJECTED);

        User admin = getCurrentAdminFromContext();

        reg.setStatus(ERegistrationStatus.REJECTED);
        reg.setRejectionReason(rejectionReason);
        reg.setReviewedBy(admin);
        reg.setReviewedAt(LocalDateTime.now());
        reg = registrationRepo.save(reg);

        log.info("Registration rejected: id={}, reason={}", registrationId, rejectionReason);

        // TODO: publish RejectionEvent → SES email to applicant
        log.info("TODO: send rejection email to {}", reg.getOwnerEmail());

        return TenantRegistrationMapper.toDto(reg);
    }

    @Override
    @Transactional
    public TenantRegistrationDto markUnderReview(UUID registrationId) {
        TenantRegistration reg = findOrThrow(registrationId);
        assertTransitionAllowed(reg, ERegistrationStatus.UNDER_REVIEW);

        reg.setStatus(ERegistrationStatus.UNDER_REVIEW);
        reg = registrationRepo.save(reg);

        log.info("Registration marked UNDER_REVIEW: id={}", registrationId);
        return TenantRegistrationMapper.toDto(reg);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private TenantRegistration findOrThrow(UUID id) {
        return registrationRepo.findById(id)
                .orElseThrow(() -> new BusinessRegistrationException(
                        "Registration not found: " + id));
    }

    /**
     * Validates that the requested state transition is legal.
     * Allowed transitions:
     *   PENDING       → UNDER_REVIEW | APPROVED | REJECTED
     *   UNDER_REVIEW  → APPROVED | REJECTED
     *   APPROVED      → (terminal — no further transitions)
     *   REJECTED      → (terminal — no further transitions)
     */
    private void assertTransitionAllowed(TenantRegistration reg, ERegistrationStatus target) {
        ERegistrationStatus current = reg.getStatus();

        boolean allowed = switch (current) {
            case PENDING      -> target == ERegistrationStatus.UNDER_REVIEW
                                 || target == ERegistrationStatus.APPROVED
                                 || target == ERegistrationStatus.REJECTED;
            case UNDER_REVIEW -> target == ERegistrationStatus.APPROVED
                                 || target == ERegistrationStatus.REJECTED;
            case APPROVED, REJECTED -> false;
        };

        if (!allowed) {
            throw new BusinessRegistrationException(
                    "Cannot transition registration from " + current + " to " + target);
        }
    }

    /**
     * Resolves the currently authenticated admin from the security context.
     * Returns null if called outside a security context (e.g. tests).
     */
    private User getCurrentAdminFromContext() {
        try {
            String email = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
            return userRepo.findByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }
}
