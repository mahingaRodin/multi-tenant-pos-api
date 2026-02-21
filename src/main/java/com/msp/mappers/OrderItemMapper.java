package com.msp.mappers;

import com.msp.models.OrderItem;
import com.msp.payloads.dtos.OrderItemDto;

public class OrderItemMapper {
    public static OrderItemDto toDto(OrderItem item) {
        if(item == null) return null;
        return OrderItemDto.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .product(item.getProduct())
                    .build();
    }
}
