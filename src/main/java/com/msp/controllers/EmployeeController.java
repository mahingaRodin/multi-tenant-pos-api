package com.msp.controllers;

import com.msp.enums.EUserRole;
import com.msp.models.User;
import com.msp.payloads.dtos.UserDto;
import com.msp.payloads.response.ApiResponse2;
import com.msp.services.EmployeeService;
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
@RequiredArgsConstructor
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "Endpoints for managing store and branch employees")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeController {
        private final EmployeeService employeeService;

        @Operation(summary = "Create a store-level employee", description = "Creates a new employee assigned to a specific store. Requires ADMIN or STORE_MANAGER role.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Store employee created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data - Email or username may already exist", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Store not found with the given ID", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
        @PostMapping("/store/{storeId}")
        public ResponseEntity<UserDto> createStoreEmployee(
                        @Parameter(name = "storeId", description = "UUID of the store where employee will work", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID storeId,

                        @Parameter(description = "Employee details to create", required = true, schema = @Schema(implementation = UserDto.class)) @Valid @RequestBody UserDto userDto)
                        throws Exception {
                UserDto employee = employeeService.createStoreEmployee(userDto, storeId);
                return ResponseEntity.ok(employee);
        }

        @Operation(summary = "Create a branch-level employee", description = "Creates a new employee assigned to a specific branch. Requires ADMIN, STORE_MANAGER, or BRANCH_MANAGER role.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Branch employee created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data - Email or username may already exist", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Branch not found with the given ID", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN','ROLE_BRANCH_MANAGER')")
        @PostMapping("/branch/{branchId}")
        public ResponseEntity<UserDto> createBranchEmployee(
                        @Parameter(name = "branchId", description = "UUID of the branch where employee will work", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID branchId,

                        @Parameter(description = "Employee details to create", required = true, schema = @Schema(implementation = UserDto.class)) @Valid @RequestBody UserDto userDto)
                        throws Exception {
                UserDto employee = employeeService.createBranchEmployee(userDto, branchId);
                return ResponseEntity.ok(employee);
        }

        @Operation(summary = "Update an employee", description = "Updates an existing employee's information. Requires ADMIN, STORE_MANAGER, or BRANCH_MANAGER role with appropriate scope.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Employee updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data or employee ID format", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Employee not found with the given ID", content = @Content),
                        @ApiResponse(responseCode = "409", description = "Email or username already in use by another employee", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN','ROLE_BRANCH_MANAGER')")
        @PutMapping("/{id}")
        public ResponseEntity<User> updateEmployee(
                        @Parameter(name = "id", description = "UUID of the employee to update", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id,

                        @Parameter(description = "Updated employee details", required = true, schema = @Schema(implementation = UserDto.class)) @Valid @RequestBody UserDto userDto)
                        throws Exception {
                User employee = employeeService.updateEmployee(id, userDto);
                return ResponseEntity.ok(employee);
        }

        @Operation(summary = "Delete an employee", description = "Deletes an employee from the system. Requires ADMIN role only.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Employee deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse2.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid employee ID format", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Employee not found with the given ID", content = @Content),
                        @ApiResponse(responseCode = "409", description = "Cannot delete employee with existing assignments or transactions", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN','ROLE_BRANCH_MANAGER')")
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse2> deleteEmployee(
                        @Parameter(name = "id", description = "UUID of the employee to delete", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id)
                        throws Exception {
                employeeService.deleteEmployee(id);
                ApiResponse2 apiResponse = new ApiResponse2();
                apiResponse.setMessage("Employee deleted");
                return ResponseEntity.ok(apiResponse);
        }

        @Operation(summary = "Get store employees", description = "Retrieves all employees for a specific store, optionally filtered by role")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Store employees retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
                        @ApiResponse(responseCode = "400", description = "Invalid store ID format", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Store not found with the given ID", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
        @GetMapping("/store/{id}")
        public ResponseEntity<Page<UserDto>> storeEmployee(
                        @Parameter(name = "id", description = "UUID of the store", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id,

                        @Parameter(name = "userRole", description = "Filter employees by role (optional)", example = "CASHIER", schema = @Schema(implementation = EUserRole.class)) @RequestParam(required = false) EUserRole userRole,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) throws Exception {
                Page<UserDto> employee = employeeService.findStoreEmployees(id, userRole, page, size);
                return ResponseEntity.ok(employee);
        }

        @Operation(summary = "Get branch employees", description = "Retrieves all employees for a specific branch, optionally filtered by role")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Branch employees retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
                        @ApiResponse(responseCode = "400", description = "Invalid branch ID format", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Branch not found with the given ID", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
        })
        @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN','ROLE_BRANCH_MANAGER')")
        @GetMapping("/branch/{id}")
        public ResponseEntity<Page<UserDto>> branchEmployee(
                        @Parameter(name = "id", description = "UUID of the branch", required = true, example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID id,

                        @Parameter(name = "userRole", description = "Filter employees by role (optional)", example = "CASHIER", schema = @Schema(implementation = EUserRole.class)) @RequestParam(required = false) EUserRole userRole,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) throws Exception {
                Page<UserDto> employee = employeeService.findBranchEmployees(id, userRole, page, size);
                return ResponseEntity.ok(employee);
        }
}