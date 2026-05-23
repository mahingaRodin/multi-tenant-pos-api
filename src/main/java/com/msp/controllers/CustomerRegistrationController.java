package com.msp.controllers;

import com.msp.payloads.dtos.CustomerDto;
import com.msp.payloads.dtos.CustomerStoreRelationshipDto;
import com.msp.payloads.dtos.CustomerUpdateDto;
import com.msp.payloads.request.CustomerRegistrationRequest;
import com.msp.payloads.response.ApiResponse2;
import com.msp.payloads.response.CustomerRegistrationResponse;
import com.msp.services.CustomerRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Customer Registration",
     description = "Global customer self-registration and store-scoped customer management")
public class CustomerRegistrationController {

    private final CustomerRegistrationService customerRegistrationService;

    // ── Public — no auth required ────────────────────────────────────────────

    @Operation(
            summary = "Register a customer account",
            description = "Creates a single global account. The customer can then browse products " +
                          "from all stores and shop anywhere. No store selection needed at registration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created",
                    content = @Content(schema = @Schema(implementation = CustomerRegistrationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/api/customers/register")
    public ResponseEntity<CustomerRegistrationResponse> register(
            @Valid @RequestBody CustomerRegistrationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerRegistrationService.register(request));
    }

    // ── Customer's own profile endpoints ─────────────────────────────────────

    @Operation(
            summary = "Get my profile",
            description = "Returns the authenticated customer's global profile."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/api/customers/{id}/profile")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<CustomerDto> getMyProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(customerRegistrationService.getCustomer(id));
    }

    @Operation(
            summary = "Get stores I've shopped at",
            description = "Returns all stores the customer has placed at least one order with."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/api/customers/{id}/stores")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<Page<CustomerStoreRelationshipDto>> getMyStores(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(customerRegistrationService.getMyStores(id, page, size));
    }

    @Operation(summary = "Update my profile")
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/api/customers/{id}/profile")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public ResponseEntity<CustomerDto> updateMyProfile(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateDto dto
    ) {
        return ResponseEntity.ok(customerRegistrationService.updateCustomer(id, dto));
    }

    // ── Store portal — store staff sees only their own customers ─────────────

    @Operation(
            summary = "List customers for a store (store portal)",
            description = "Returns all customers who have placed at least one order at this store. " +
                          "A store can only see customers who have interacted with them."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customer list returned"),
            @ApiResponse(responseCode = "404", description = "Store not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/api/portal/stores/{storeId}/customers")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER','ROLE_SUPER_ADMIN')")
    public ResponseEntity<Page<CustomerStoreRelationshipDto>> getStoreCustomers(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                customerRegistrationService.getCustomersByStore(storeId, page, size));
    }

    @Operation(
            summary = "Search customers in a store (store portal)",
            description = "Searches by name or email within the store's customer base."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/api/portal/stores/{storeId}/customers/search")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_MANAGER','ROLE_BRANCH_CASHIER','ROLE_SUPER_ADMIN')")
    public ResponseEntity<Page<CustomerStoreRelationshipDto>> searchStoreCustomers(
            @PathVariable UUID storeId,
            @Parameter(description = "Search keyword (name or email)") @RequestParam String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                customerRegistrationService.searchCustomersByStore(storeId, q, page, size));
    }

    @Operation(
            summary = "Get a specific customer in store context (store portal)",
            description = "Returns the customer's profile enriched with store-specific data " +
                          "(first interaction date, notes, etc.)."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/api/portal/stores/{storeId}/customers/{customerId}")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_MANAGER','ROLE_BRANCH_CASHIER','ROLE_SUPER_ADMIN')")
    public ResponseEntity<CustomerStoreRelationshipDto> getCustomerInStore(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId
    ) {
        return ResponseEntity.ok(
                customerRegistrationService.getCustomerInStore(customerId, storeId));
    }

    @Operation(
            summary = "Update store notes for a customer (store portal)",
            description = "Lets store staff add or update private notes about a customer " +
                          "(e.g. VIP status, preferences). Does not modify the customer's global profile."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @PatchMapping("/api/portal/stores/{storeId}/customers/{customerId}/notes")
    @PreAuthorize("hasAnyRole('ROLE_STORE_ADMIN','ROLE_STORE_MANAGER','ROLE_SUPER_ADMIN')")
    public ResponseEntity<CustomerStoreRelationshipDto> updateStoreNotes(
            @PathVariable UUID storeId,
            @PathVariable UUID customerId,
            @RequestParam String notes
    ) {
        return ResponseEntity.ok(
                customerRegistrationService.updateStoreNotes(customerId, storeId, notes));
    }

    // ── Super admin ──────────────────────────────────────────────────────────

    @Operation(
            summary = "Delete a customer account (super admin)",
            description = "Permanently removes the customer's global account and all store relationships."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/api/customers/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse2> deleteCustomer(@PathVariable UUID id) {
        customerRegistrationService.deleteCustomer(id);
        ApiResponse2 response = new ApiResponse2();
        response.setMessage("Customer account deleted successfully.");
        return ResponseEntity.ok(response);
    }
}
