package com.msp.payloads.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    private UUID id;
    private BranchDto branch;
    private UUID branchId;
    private ProductDto product;
    private UUID productId;
    private Integer quantity;
    private LocalTime lastUpdate;
}
