package com.msp.controllers;

import com.msp.models.Inventory;
import com.msp.payloads.dtos.InventoryDto;
import com.msp.payloads.response.ApiResponse;
import com.msp.services.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryDto> createInventory(
            @RequestBody InventoryDto inventoryDto
    ) throws Exception {
        InventoryDto created = inventoryService.createInventory(inventoryDto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable UUID id,
            @RequestBody InventoryDto inventoryDto
            ) throws Exception {
        InventoryDto updated = inventoryService.updateInventory(id,inventoryDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteInventory(
            @PathVariable UUID id
    ) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Inventory deleted Successfully!");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/branch/{branchId}/product/{productId}")
    public ResponseEntity<InventoryDto> getInventoryByProductAndBranchId(
            @PathVariable UUID branchId,
            @PathVariable UUID productId
    ) throws Exception {
        return ResponseEntity.ok(
                inventoryService.getInventoryByProductIdAndBranchId(productId,branchId)
        );
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<InventoryDto>> getInventoryBranch(
            @PathVariable UUID branchId
    ) throws Exception {
        return ResponseEntity.ok(
                inventoryService.getAlInventoryByBranchId(branchId)
        );
    }
}
