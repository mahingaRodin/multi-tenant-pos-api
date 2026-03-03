package com.msp.controllers;

import com.msp.payloads.dtos.CustomerDto;
import com.msp.payloads.dtos.CustomerUpdateDto;
import com.msp.payloads.response.ApiResponse2;
import com.msp.services.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Endpoints for managing customer information")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {
        private final CustomerService customerService;

        @Operation(summary = "Create a new customer", description = "Creates a new customer record with the provided details. Requires CASHIER, MANAGER, or ADMIN role.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Customer created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data - Email or phone may already exist or validation failed", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions", content = @Content),
                        @ApiResponse(responseCode = "409", description = "Customer with this email or phone already exists", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_BRANCH_MANAGER','ROLE_BRANCH_CASHIER', 'ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
        @PostMapping
        public ResponseEntity<CustomerDto> createCustomer(
                        @Parameter(description = "Customer details to create", required = true, schema = @Schema(implementation = CustomerDto.class)) @Valid @RequestBody CustomerDto customerDto)
                        throws Exception {
                return ResponseEntity.ok(customerService.createCustomer(customerDto));
        }

        @Operation(summary = "Partially update a customer", description = "Updates specific fields of an existing customer identified by ID. Requires CASHIER, MANAGER, or ADMIN role.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Customer updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data or customer ID format", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Customer not found with the given ID", content = @Content),
                        @ApiResponse(responseCode = "409", description = "Email or phone already in use by another customer", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_BRANCH_MANAGER','ROLE_BRANCH_CASHIER', 'ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
        @PatchMapping("/{id}")
        public ResponseEntity<CustomerDto> patchCustomer(
                        @Parameter(name = "id", description = "UUID of the customer to update", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id,

                        @Parameter(description = "Customer fields to update (only non-null fields will be updated)", required = true, schema = @Schema(implementation = CustomerUpdateDto.class)) @Valid @RequestBody CustomerUpdateDto dto)
                        throws Exception {
                return ResponseEntity.ok(customerService.patchCustomer(id, dto));
        }

        @Operation(summary = "Delete a customer", description = "Deletes an existing customer identified by ID. Requires ADMIN role only.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Customer deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse2.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid customer ID format", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Customer not found with the given ID", content = @Content),
                        @ApiResponse(responseCode = "409", description = "Cannot delete customer with existing orders or transactions", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_BRANCH_MANAGER','ROLE_BRANCH_CASHIER', 'ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse2> deleteCustomer(
                        @Parameter(name = "id", description = "UUID of the customer to delete", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id)
                        throws Exception {
                customerService.deleteCustomer(id);

                ApiResponse2 apiResponse = new ApiResponse2();
                apiResponse.setMessage("Customer Deleted Successfully!");
                return ResponseEntity.ok(apiResponse);
        }

        @Operation(summary = "Get customer by ID", description = "Retrieves detailed information of a specific customer by their ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Customer found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid customer ID format", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Customer not found with the given ID", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_BRANCH_MANAGER','ROLE_BRANCH_CASHIER', 'ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
        @GetMapping("/{id}")
        public ResponseEntity<CustomerDto> getCustomer(
                        @Parameter(name = "id", description = "UUID of the customer to retrieve", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id)
                        throws Exception {
                return ResponseEntity.ok(customerService.getCustomer(id));
        }

        @Operation(summary = "Get all customers", description = "Retrieves a list of all customers in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Customers retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CustomerDto.class)))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_BRANCH_MANAGER','ROLE_BRANCH_CASHIER', 'ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
        @GetMapping
        public ResponseEntity<Page<CustomerDto>> getAllCustomers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) throws Exception {
                return ResponseEntity.ok(customerService.getAllCustomers(page, size));
        }

        @Operation(summary = "Search customers", description = "Searches for customers by name, email, phone, or other criteria")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CustomerDto.class)))),
                        @ApiResponse(responseCode = "400", description = "Invalid search query parameter", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @GetMapping("/search")
        @PreAuthorize("hasAnyAuthority('ROLE_BRANCH_MANAGER','ROLE_BRANCH_CASHIER', 'ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
        public ResponseEntity<Page<CustomerDto>> search(
                        @Parameter(name = "q", description = "Search query string (searches across name, email, phone, etc.)", required = true, example = "john", schema = @Schema(type = "string", minLength = 2)) @RequestParam String q,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) throws Exception {
                return ResponseEntity.ok(customerService.searchCustomers(q, page, size));
        }
}