package com.msp.impls;

import com.msp.enums.EOrderStatus;
import com.msp.enums.EPaymentType;
import com.msp.mappers.OrderMapper;
import com.msp.models.*;
import com.msp.payloads.dtos.OrderDto;
import com.msp.repositories.BranchRepository;
import com.msp.repositories.CustomerRepository;
import com.msp.repositories.OrderRepository;
import com.msp.repositories.ProductRepository;
import com.msp.services.OrderService;
import com.msp.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "orders")
public class OrderServiceImpl implements OrderService {
        private final OrderRepository orderRepository;
        private final UserService userService;
        private final ProductRepository productRepository;
        private final CustomerRepository customerRepository;
        private final BranchRepository branchRepository;

        @Override
        @Caching(put = {
                @CachePut(key = "#result.id")
        }, evict = {
                @CacheEvict(value = "orders-by-branch", allEntries = true),
                @CacheEvict(value = "orders-by-cashier", allEntries = true),
                @CacheEvict(value = "orders-by-customer", allEntries = true),
                @CacheEvict(value = "orders-today", allEntries = true),
                @CacheEvict(value = "orders-recent", allEntries = true),
                @CacheEvict(value = "orders-all", allEntries = true)
        })
        public OrderDto createOrder(OrderDto orderDto) throws Exception {
                User cashier = userService.getCurrentUser();
                System.out.println("current_user: " + cashier.getId());
                System.out.println("Cashier branch: "
                        + (cashier.getBranch() != null ? cashier.getBranch().getId() : null));

                Branch branch = branchRepository.findById(orderDto.getBranchId()).orElseThrow(
                        () -> new EntityNotFoundException("Branch not found"));

                if (orderDto.getCustomerId() == null) {
                        throw new Exception("customerId is required");
                }

                Customer customer = customerRepository.findById(orderDto.getCustomerId())
                        .orElseThrow(() -> new EntityNotFoundException("Customer Not Found!"));

                Order order = Order.builder()
                        .branch(branch)
                        .cashier(cashier)
                        .customer(customer)
                        .paymentType(orderDto.getPaymentType())
                        .build();

                List<OrderItem> orderItems = orderDto.getItems().stream().map(itemDto -> {
                        Product product = productRepository.findById(itemDto.getProductId())
                                .orElseThrow(() -> new EntityNotFoundException("Product Not Found!"));

                        return OrderItem.builder()
                                .product(product)
                                .quantity(itemDto.getQuantity())
                                .price(product.getSellingPrice() * itemDto.getQuantity())
                                .order(order)
                                .build();
                }).toList();

                double total = orderItems.stream().mapToDouble(OrderItem::getPrice).sum();

                order.setTotalAmount(total);
                order.setItems(orderItems);

                Order savedOrder = orderRepository.save(order);

                return OrderMapper.toDto(savedOrder);
        }

        @Override
        @Cacheable(key = "#id")
        public OrderDto getOrderById(UUID id) throws Exception {
                return orderRepository.findById(id)
                        .map(OrderMapper::toDto)
                        .orElseThrow(() -> new Exception("Order not Found with id: " + id));
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "orders-by-branch", key = "#branchId + '-' + #customerId + '-' + #cashierId + '-' + #paymentType + '-' + #orderStatus + '-' + #page + '-' + #size")
        public Page<OrderDto> getOrdersByBranch(UUID branchId,
                                                UUID customerId,
                                                UUID cashierId,
                                                EPaymentType paymentType,
                                                EOrderStatus orderStatus,
                                                int page,
                                                int size) throws Exception {
                Pageable pageable = PageRequest.of(page, size);
                Page<Order> orderPage = orderRepository.findByBranchId(branchId, pageable);

                List<OrderDto> orders = orderPage.stream()
                        .filter(order -> customerId == null || (order.getCustomer() != null && order.getCustomer().getId().equals(customerId)))
                        .filter(order -> cashierId == null || (order.getCashier() != null && order.getCashier().getId().equals(cashierId)))
                        .filter(order -> paymentType == null || order.getPaymentType() == paymentType)
                        .map(OrderMapper::toDto)
                        .collect(Collectors.toList());

                return new PageImpl<>(orders, pageable, orderPage.getTotalElements());
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "orders-by-cashier", key = "#cashierId + '-' + #page + '-' + #size")
        public Page<OrderDto> getOrderByCashier(UUID cashierId, int page, int size) {
                Pageable pageable = PageRequest.of(page, size);
                return orderRepository.findByCashier_Id(cashierId, pageable)
                        .map(OrderMapper::toDto);
        }

        @Override
        @Caching(evict = {
                @CacheEvict(key = "#id"),
                @CacheEvict(value = "orders-by-branch", allEntries = true),
                @CacheEvict(value = "orders-by-cashier", allEntries = true),
                @CacheEvict(value = "orders-by-customer", allEntries = true),
                @CacheEvict(value = "orders-today", allEntries = true),
                @CacheEvict(value = "orders-recent", allEntries = true),
                @CacheEvict(value = "orders-all", allEntries = true)
        })
        public void deleteOrder(UUID id) throws Exception {
                Order order = orderRepository.findById(id).orElseThrow(
                        () -> new Exception("Order not Found with id: " + id));
                orderRepository.delete(order);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "orders-today", key = "#branchId + '-' + #page + '-' + #size")
        public Page<OrderDto> getTodayOrderByBranch(UUID branchId, int page, int size) throws Exception {
                LocalDate today = LocalDate.now();
                LocalDateTime start = today.atStartOfDay();
                LocalDateTime end = today.plusDays(1).atStartOfDay();
                Pageable pageable = PageRequest.of(page, size);
                return orderRepository.findByBranchIdAndCreatedAtBetween(branchId, start, end, pageable)
                        .map(OrderMapper::toDto);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "orders-by-customer", key = "#customerId + '-' + #page + '-' + #size")
        public Page<OrderDto> getOrderByCustomerId(UUID customerId, int page, int size) throws Exception {
                Pageable pageable = PageRequest.of(page, size);
                return orderRepository.findByCustomerId(customerId, pageable)
                        .map(OrderMapper::toDto);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "orders-recent", key = "#branchId + '-' + #page + '-' + #size")
        public Page<OrderDto> getTop5RecentOrdersByBranchId(UUID branchId, int page, int size) throws Exception {
                Pageable pageable = PageRequest.of(page, size);
                return orderRepository.findTop5ByBranchIdOrderByCreatedAtDesc(branchId, pageable)
                        .map(OrderMapper::toDto);
        }

        @Override
        @Transactional(readOnly = true)
        @Cacheable(value = "orders-all", key = "#page + '-' + #size")
        public Page<OrderDto> getAllOrders(int page, int size) throws Exception {
                Pageable pageable = PageRequest.of(page, size);
                return orderRepository.findAll(pageable).map(OrderMapper::toDto);
        }

        @Override
        @Caching(put = {@CachePut(key = "#id")},
                evict = {
                        @CacheEvict(value = "orders-by-branch", allEntries = true),
                        @CacheEvict(value = "orders-by-cashier", allEntries = true),
                        @CacheEvict(value = "orders-by-customer", allEntries = true),
                        @CacheEvict(value = "orders-today", allEntries = true),
                        @CacheEvict(value = "orders-recent", allEntries = true),
                        @CacheEvict(value = "orders-all", allEntries = true)
                })
        public OrderDto updateOrder(UUID id, OrderDto orderDto) throws Exception {
                Order existingOrder = orderRepository.findById(id)
                        .orElseThrow(() -> new Exception("Order not found with id: " + id));

                if (orderDto.getPaymentType() != null) {
                        existingOrder.setPaymentType(orderDto.getPaymentType());
                }
                if (orderDto.getBranchId() != null && !orderDto.getBranchId().equals(existingOrder.getBranch().getId())) {
                        Branch newBranch = branchRepository.findById(orderDto.getBranchId())
                                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));
                        existingOrder.setBranch(newBranch);
                }

                Order updated = orderRepository.save(existingOrder);
                return OrderMapper.toDto(updated);
        }

        @Override
        @Caching(put = {@CachePut(key = "#id")},
                evict = {
                        @CacheEvict(value = "orders-by-branch", allEntries = true),
                        @CacheEvict(value = "orders-today", allEntries = true),
                        @CacheEvict(value = "orders-recent", allEntries = true),
                        @CacheEvict(value = "orders-all", allEntries = true)
                })
        public OrderDto updateOrderStatus(UUID id, EOrderStatus status) throws Exception {
                Order order = orderRepository.findById(id)
                        .orElseThrow(() -> new Exception("Order not found with id: " + id));
                Order updated = orderRepository.save(order);
                return OrderMapper.toDto(updated);
        }
}