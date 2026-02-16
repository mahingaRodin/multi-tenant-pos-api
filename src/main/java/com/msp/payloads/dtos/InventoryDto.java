package com.msp.payloads.dtos;

import com.msp.models.Branch;
import com.msp.models.Product;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class InventoryDto {
    private UUID id;
    private Branch branch;
    private Product product;
    private Integer quantity;
    private LocalTime lastUpdate;
}
