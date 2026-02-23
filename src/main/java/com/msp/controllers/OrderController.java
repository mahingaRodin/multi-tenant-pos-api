package com.msp.controllers;

import com.msp.enums.EOrderStatus;
import com.msp.enums.EPaymentType;
import com.msp.payloads.dtos.OrderDto;
import com.msp.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @RequestBody OrderDto orderDto
    ) throws Exception {
        return ResponseEntity.ok(orderService.createOrder(orderDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable UUID id
            ) throws Exception {
        return  ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<OrderDto>> getOrdersByBranchId(
            @PathVariable UUID branchId,
            @RequestParam(required=false) UUID customerId,
            @RequestParam(required=false) UUID cashierId,
            @RequestParam(required = false)EPaymentType paymentType,
            @RequestParam(required = false)EOrderStatus orderStatus
            ) throws Exception {
        return ResponseEntity.ok(orderService.getOrdersByBranch(branchId,customerId,cashierId,paymentType,orderStatus));
    }

    @GetMapping("/cashier/{id}")
    public ResponseEntity<List<OrderDto>> getOrderByCashier(
            @PathVariable UUID id
    ) throws Exception {
        return ResponseEntity.ok(orderService.getOrderByCashier(id));
    }

    @GetMapping("/today/branch/{id}")
    public ResponseEntity<List<OrderDto>> getTodayOrder(
            @PathVariable UUID id
    ) throws Exception {
        return ResponseEntity.ok(orderService.getTodayOrderByBranch(id));
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<List<OrderDto>> getCustomerOrder(
            @PathVariable UUID id
    ) throws Exception {
        return ResponseEntity.ok(orderService.getOrderByCustomerId(id));
    }

    @GetMapping("/recent/branch/{branchId}")
    public ResponseEntity<List<OrderDto>> getRecentOrder(
            @PathVariable UUID branchId
    ) throws Exception {
        return ResponseEntity.ok(orderService.getTop5RecentOrdersByBranchId(branchId));
    }

 }
