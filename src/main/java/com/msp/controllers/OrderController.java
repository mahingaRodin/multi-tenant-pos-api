package com.msp.controllers;

import com.msp.enums.EOrderStatus;
import com.msp.enums.EPaymentType;
import com.msp.payloads.dtos.OrderDto;
import com.msp.services.OrderService;
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
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "Endpoints for managing customer orders and transactions")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {
    private final OrderService orderService;

    @Operation(
            summary = "Create a new order",
            description = "Creates a new customer order with items. Requires CASHIER role. Automatically updates inventory."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data - Insufficient inventory or validation failed",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - CASHIER role required",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product, Branch, or Customer not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @Parameter(
                    description = "Order details including items, payment info, and customer details",
                    required = true,
                    schema = @Schema(implementation = OrderDto.class)
            )
            @Valid @RequestBody OrderDto orderDto
    ) throws Exception {
        return ResponseEntity.ok(orderService.createOrder(orderDto));
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieves detailed information of a specific order by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found with the given ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @Parameter(
                    name = "id",
                    description = "UUID of the order to retrieve",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id
    ) throws Exception {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(
            summary = "Get orders by branch with filters",
            description = "Retrieves orders for a specific branch with optional filtering by customer, cashier, payment type, and order status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid branch ID format or filter parameters",
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
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<Page<OrderDto>> getOrdersByBranchId(
            @Parameter(
                    name = "branchId",
                    description = "UUID of the branch",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID branchId,

            @Parameter(
                    name = "customerId",
                    description = "Filter by customer ID (optional)",
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestParam(required = false) UUID customerId,

            @Parameter(
                    name = "cashierId",
                    description = "Filter by cashier ID (optional)",
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestParam(required = false) UUID cashierId,

            @Parameter(
                    name = "paymentType",
                    description = "Filter by payment type (optional)",
                    example = "CASH",
                    schema = @Schema(implementation = EPaymentType.class)
            )
            @RequestParam(required = false) EPaymentType paymentType,

            @Parameter(
                    name = "orderStatus",
                    description = "Filter by order status (optional)",
                    example = "COMPLETED",
                    schema = @Schema(implementation = EOrderStatus.class)
            )
            @RequestParam(required = false) EOrderStatus orderStatus,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size
    ) throws Exception {
        return ResponseEntity.ok(orderService.getOrdersByBranch(branchId, customerId, cashierId, paymentType, orderStatus,page,size));
    }

    @Operation(
            summary = "Get orders by cashier",
            description = "Retrieves all orders processed by a specific cashier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid cashier ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cashier not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_ADMIN','ROLE_SUPER_ADMIN','ROLE_BRANCH_MANAGER','ROLE_STORE_MANAGER')")
    @GetMapping("/cashier/{id}")
    public ResponseEntity<Page<OrderDto>> getOrderByCashier(
            @Parameter(
                    name = "id",
                    description = "UUID of the cashier",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size
    ) throws Exception {
        return ResponseEntity.ok(orderService.getOrderByCashier(id,page,size));
    }

    @Operation(
            summary = "Get today's orders by branch",
            description = "Retrieves all orders created today for a specific branch"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Today's orders retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderDto.class))
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
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_ADMIN','ROLE_SUPER_ADMIN','ROLE_BRANCH_MANAGER','ROLE_STORE_MANAGER')")
    @GetMapping("/today/branch/{id}")
    public ResponseEntity<Page<OrderDto>> getTodayOrder(
            @Parameter(
                    name = "id",
                    description = "UUID of the branch",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10") int size
    ) throws Exception {
        return ResponseEntity.ok(orderService.getTodayOrderByBranch(id,page,size));
    }

    @Operation(
            summary = "Get orders by customer",
            description = "Retrieves all orders placed by a specific customer"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer orders retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid customer ID format",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing JWT token",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_ADMIN','ROLE_SUPER_ADMIN','ROLE_BRANCH_MANAGER','ROLE_STORE_MANAGER','ROLE_CUSTOMER')")
    @GetMapping("/customer/{id}")
    public ResponseEntity<Page<OrderDto>> getCustomerOrder(
            @Parameter(
                    name = "id",
                    description = "UUID of the customer",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size
    ) throws Exception {
        return ResponseEntity.ok(orderService.getOrderByCustomerId(id,page,size));
    }

    @Operation(
            summary = "Get recent orders by branch",
            description = "Retrieves the 5 most recent orders for a specific branch"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Recent orders retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderDto.class))
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
    @PreAuthorize("hasAnyAuthority('ROLE_STORE_ADMIN','ROLE_SUPER_ADMIN','ROLE_BRANCH_MANAGER','ROLE_STORE_MANAGER')")
    @GetMapping("/recent/branch/{branchId}")
    public ResponseEntity<Page<OrderDto>> getRecentOrder(
            @Parameter(
                    name = "branchId",
                    description = "UUID of the branch",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID branchId,
            @RequestParam(defaultValue = "0")int page,
            @RequestParam(defaultValue = "10")int size
    ) throws Exception {
        return ResponseEntity.ok(orderService.getTop5RecentOrdersByBranchId(branchId,page,size));
    }
}