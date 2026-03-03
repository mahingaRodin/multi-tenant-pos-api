package com.msp.controllers;

import com.msp.models.User;
import com.msp.payloads.dtos.ProductDto;
import com.msp.payloads.response.ApiResponse2;
import com.msp.services.ProductService;
import com.msp.services.UserService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "Endpoints for managing products in the system")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {
    private final ProductService productService;
    private final UserService userService;

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product with the provided details. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data - Product code may already exist or validation failed",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions (ADMIN or MANAGER required)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category or Store not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_ADMIN','ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER')")
    @PostMapping
    public ResponseEntity<ProductDto> create(
            @Parameter(
                    description = "Product details to create",
                    required = true,
                    schema = @Schema(implementation = ProductDto.class)
            )
            @Valid @RequestBody ProductDto productDto,

            @Parameter(
                    description = "Bearer JWT token for authentication",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    schema = @Schema(type = "string", format = "Bearer [token]")
            )
            @RequestHeader("Authorization") String token
    ) throws Exception {
        User user = userService.getCurrentUserFromToken(token);
        return ResponseEntity.ok(
                productService.createProduct(
                        productDto, user
                )
        );
    }

    @Operation(
            summary = "Get all products by store",
            description = "Retrieves a list of all products associated with a specific store"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid store ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Store not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_ADMIN','ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_MANAGER')")
    @GetMapping("/store/{storeId}")
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @Parameter(
                    name = "storeId",
                    description = "UUID of the store to retrieve products for",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size
    ) {
        return ResponseEntity.ok(productService.getProductsByStoreId(storeId,page,size));
    }

    @Operation(
            summary = "Search products by keyword",
            description = "Searches for products within a specific store using a keyword"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid store ID format or empty search keyword",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Store not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_ADMIN','ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_MANAGER')")
    @GetMapping("/store/{storeId}/search")
    public ResponseEntity<Page<ProductDto>> searchByKeyword(
            @Parameter(
                    name = "storeId",
                    description = "UUID of the store to search in",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID storeId,

            @Parameter(
                    name = "keyword",
                    description = "Search keyword (searches in product name, description, and code)",
                    required = true,
                    example = "laptop",
                    schema = @Schema(type = "string", minLength = 2)
            )
            @RequestParam String keyword,

            @Parameter(
                    description = "Bearer JWT token for authentication",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    schema = @Schema(type = "string", format = "Bearer [token]")
            )
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size
    ) throws Exception {
        return ResponseEntity.ok(
                productService.searchByKeyword(
                        storeId,
                        keyword,
                        page,
                        size
                )
        );
    }

    @Operation(
            summary = "Update a product",
            description = "Updates an existing product identified by ID. Requires ADMIN or MANAGER role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or product ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions (ADMIN or MANAGER required)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product not found with the given ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Product code already exists for another product",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_ADMIN','ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_MANAGER')")
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @Parameter(
                    name = "id",
                    description = "UUID of the product to update",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id,

            @Parameter(
                    description = "Updated product details (only provided fields will be updated)",
                    required = true,
                    schema = @Schema(implementation = ProductDto.class)
            )
            @Valid @RequestBody ProductDto productDto,

            @Parameter(
                    description = "Bearer JWT token for authentication",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    schema = @Schema(type = "string", format = "Bearer [token]")
            )
            @RequestHeader("Authorization") String token
    ) throws Exception {
        User user = userService.getCurrentUserFromToken(token);
        return ResponseEntity.ok(
                productService.updateProduct(
                        id, productDto, user
                )
        );
    }

    @Operation(
            summary = "Delete a product",
            description = "Deletes an existing product identified by ID. Requires ADMIN role only."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse2.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product ID format",
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
                    description = "Product not found with the given ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot delete product with existing inventory or orders",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_ADMIN','ROLE_SUPER_ADMIN','ROLE_STORE_MANAGER','ROLE_BRANCH_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse2> deleteProduct(
            @Parameter(
                    name = "id",
                    description = "UUID of the product to delete",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id,

            @Parameter(
                    description = "Bearer JWT token for authentication",
                    required = true,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    schema = @Schema(type = "string", format = "Bearer [token]")
            )
            @RequestHeader("Authorization") String token
    ) throws Exception {
        User user = userService.getCurrentUserFromToken(token);
        productService.deleteProduct(id, user);

        ApiResponse2 apiResponse = new ApiResponse2();
        apiResponse.setMessage("Product Deleted Successfully!");
        return ResponseEntity.ok(apiResponse);
    }
}