package com.msp.controllers;

import com.msp.payloads.dtos.RefundDto;
import com.msp.services.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/refunds")
public class RefundController {
    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundDto> createRefund(
            @RequestBody RefundDto refundDto
    ) throws Exception {
        RefundDto refund = refundService.createRefund(refundDto);
        return ResponseEntity.ok(refund);
    }

    @GetMapping
    public ResponseEntity<List<RefundDto>> getAllRefunds() throws Exception {
        List<RefundDto> refunds = refundService.getAllRefunds();
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/cashier/{cashierId}")
    public ResponseEntity<List<RefundDto>> getRefundByCashierId(
            @PathVariable UUID cashierId
            ) throws Exception {
        List<RefundDto> refunds = refundService.getRefundByCashier(cashierId);
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<RefundDto>> getRefundByBranchId(
            @PathVariable UUID branchId
    ) throws Exception {
        List<RefundDto> refunds = refundService.getRefundByBranch(branchId);
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/shift/{shiftId}")
    public ResponseEntity<List<RefundDto>> getRefundByShiftId(
            @PathVariable UUID shiftId
    ) throws Exception {
        List<RefundDto> refunds = refundService.getRefundByShiftReport(shiftId);
        return ResponseEntity.ok(refunds);
    }

    @GetMapping("/cashier/{cashierId}/range")
    public ResponseEntity<List<RefundDto>> getRefundByCashierAndDateRange(
            @PathVariable UUID cashierId,
            @RequestParam @DateTimeFormat (iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime startDate,
            @RequestParam @DateTimeFormat (iso= DateTimeFormat.ISO.DATE_TIME)LocalDateTime endDate
            ) throws Exception {
        List<RefundDto> refund = refundService.getRefundByCashierAndDateRange(
                cashierId,
                startDate,
                endDate
        );
        return ResponseEntity.ok(refund);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundDto> getRefundById(
            @PathVariable UUID refundId
    ) throws Exception {
        RefundDto refund = refundService.getRefundById(refundId);
        return ResponseEntity.ok(refund);
    }
}
