package com.msp.payloads.dtos;

import com.msp.models.Store;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProductDto {
    private UUID id;
    private String name;
    private String sku;
    private String description;
    private Double mrp;
    private Double sellingPrice;
    private String brand;
    private String image;
    private UUID categoryId;
    private UUID storeId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
