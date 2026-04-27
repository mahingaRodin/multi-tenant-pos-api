package com.msp.services;

import com.msp.enums.EOrderStatus;
import com.msp.enums.EPaymentType;
import com.msp.payloads.dtos.OrderDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto) throws Exception;
    OrderDto getOrderById(UUID id) throws Exception;
    Page<OrderDto> getOrdersByBranch(UUID branchId,
                                     UUID customerId,
                                     UUID cashierId,
                                     EPaymentType paymentType,
                                     EOrderStatus orderStatus,
                                     int page,
                                     int size
                                     ) throws Exception;
    Page<OrderDto> getOrderByCashier(UUID cashierId,int page, int size);
    void deleteOrder(UUID id) throws Exception;
    Page<OrderDto> getTodayOrderByBranch(UUID branchId, int page, int size) throws Exception;
    Page<OrderDto> getOrderByCustomerId(UUID customerId,int page, int size) throws Exception;
    Page<OrderDto> getTop5RecentOrdersByBranchId(UUID branchId, int page, int size) throws Exception;
    Page<OrderDto> getAllOrders(int page, int size) throws Exception;
    OrderDto updateOrder(UUID id, OrderDto orderDto) throws Exception;
    OrderDto updateOrderStatus(UUID id, EOrderStatus status) throws Exception;
}
