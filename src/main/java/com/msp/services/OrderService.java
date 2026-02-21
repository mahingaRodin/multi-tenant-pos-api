package com.msp.services;

import com.msp.enums.EOrderStatus;
import com.msp.enums.EPaymentType;
import com.msp.payloads.dtos.OrderDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto) throws Exception;
    OrderDto getOrderById(UUID id) throws Exception;
    List<OrderDto> getOrdersByBranch(UUID branchId,
                                     UUID customerId,
                                     UUID cashierId,
                                     EPaymentType paymentType,
                                     EOrderStatus orderStatus
                                     ) throws Exception;
    List<OrderDto> getOrderByCashier(UUID cashierId);
    void deleteOrder(UUID id) throws Exception;
    List<OrderDto> getTodayOrderByBranch(UUID branchId) throws Exception;
    List<OrderDto> getOrderByCustomerId(UUID customerId) throws Exception;
    List<OrderDto> getTop5RecentOrdersByBranchId(UUID branchId) throws Exception;
}
