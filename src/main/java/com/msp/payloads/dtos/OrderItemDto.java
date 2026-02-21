package com.msp.payloads.dtos;

import com.msp.models.Product;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderItemDto {
    private UUID id;
    private Integer quantity;
    private Double price;
    private Product product;
    private UUID productId;
    private UUID orderId;
}
