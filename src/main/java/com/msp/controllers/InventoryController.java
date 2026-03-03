package com.msp.controllers;

import com.msp.payloads.dtos.InventoryDto;
import com.msp.payloads.response.ApiResponse2;
import com.msp.services.InventoryService;
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
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "Endpoints for managing product inventory across branches")
@SecurityRequirement(name = "Bearer Authentication")
public class InventoryController {
    private final InventoryService inventoryService;

    @Operation(
            summary = "Create inventory record",
            description = "Creates a new inventory record for a product in a specific branch. Requires ADMIN, MANAGER, or CASHIER role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventoryDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data - Quantity must be non-negative or validation failed",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product or Branch not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Inventory record already exists for this product and branch",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
    @PostMapping
    public ResponseEntity<InventoryDto> createInventory(
            @Parameter(
                    description = "Inventory details including product ID, branch ID, and quantity",
                    required = true,
                    schema = @Schema(implementation = InventoryDto.class)
            )
            @Valid @RequestBody InventoryDto inventoryDto
    ) throws Exception {
        InventoryDto created = inventoryService.createInventory(inventoryDto);
        return ResponseEntity.ok(created);
    }

    @Operation(
            summary = "Update inventory record",
            description = "Updates an existing inventory record identified by ID. Requires ADMIN, MANAGER, or CASHIER role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventoryDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or inventory ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory record not found with the given ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> updateInventory(
            @Parameter(
                    name = "id",
                    description = "UUID of the inventory record to update",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id,

            @Parameter(
                    description = "Updated inventory details",
                    required = true,
                    schema = @Schema(implementation = InventoryDto.class)
            )
            @Valid @RequestBody InventoryDto inventoryDto
    ) throws Exception {
        InventoryDto updated = inventoryService.updateInventory(id, inventoryDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete inventory record",
            description = "Deletes an inventory record by its ID. Requires ADMIN role only."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse2.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid inventory ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - ADMIN role required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory record not found with the given ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse2> deleteInventory(
            @Parameter(
                    name = "id",
                    description = "UUID of the inventory record to delete",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id
    ) throws Exception {
        inventoryService.deleteInventory(id);
        ApiResponse2 apiResponse = new ApiResponse2();
        apiResponse.setMessage("Inventory deleted Successfully!");
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(
            summary = "Get inventory by product and branch",
            description = "Retrieves inventory details for a specific product in a specific branch"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InventoryDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid branch ID or product ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Inventory not found for the given product and branch combination",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN','ROLE_BRANCH_MANAGER')")
    @GetMapping("/branch/{branchId}/product/{productId}")
    public ResponseEntity<InventoryDto> getInventoryByProductAndBranchId(
            @Parameter(
                    name = "branchId",
                    description = "UUID of the branch",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID branchId,

            @Parameter(
                    name = "productId",
                    description = "UUID of the product",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID productId
    ) throws Exception {
        return ResponseEntity.ok(
                inventoryService.getInventoryByProductIdAndBranchId(productId, branchId)
        );
    }

    @Operation(
            summary = "Get all inventory by branch",
            description = "Retrieves all inventory records for a specific branch"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Inventory records retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = InventoryDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid branch ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Branch not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_STORE_ADMIN','ROLE_BRANCH_MANAGER')")
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<Page<InventoryDto>> getInventoryBranch(
            @Parameter(
                    name = "branchId",
                    description = "UUID of the branch to retrieve inventory for",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID branchId,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size
    ) throws Exception {
        return ResponseEntity.ok(
                inventoryService.getAllInventoryByBranchId(branchId,page,size)
        );
    }
}