package com.msp.impls;

import com.msp.enums.EOrderStatus;
import com.msp.enums.EPaymentType;
import com.msp.mappers.CustomerMapper;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;

    @Override
    public OrderDto createOrder(OrderDto orderDto) throws Exception {
        User cashier = userService.getCurrentUser();
        System.out.println("current_user: " +cashier.getId());
        System.out.println("Cashier branch: " + (cashier.getBranch() != null ? cashier.getBranch().getId() : null));
//        Branch branch = cashier.getBranch();
//        if (branch == null) throw new Exception("Branch Not Found!");
        Branch branch = branchRepository.findById(orderDto.getBranchId()).orElseThrow(
                () -> new EntityNotFoundException("Branch not found")
        );

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
    public OrderDto getOrderById(UUID id) throws Exception {
        return orderRepository.findById(id)
                .map(OrderMapper::toDto)
                .orElseThrow(
                        () -> new Exception("Order not Found with id: "+id)
                );
    }

    @Override
    public List<OrderDto> getOrdersByBranch(UUID branchId,
                                            UUID customerId,
                                            UUID cashierId,
                                            EPaymentType paymentType,
                                            EOrderStatus orderStatus) throws Exception {
        return orderRepository.findByBranchId(branchId).stream()
                .filter(order -> customerId == null ||
                        (order.getCustomer() != null &&
                                order.getCustomer().getId().equals(customerId)))
                .filter(order -> cashierId==null ||
                        order.getCashier()!=null &&
                                order.getCashier().getId().equals(customerId))
                .filter(order -> paymentType==null ||
                order.getPaymentType()==paymentType)
                .map(OrderMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrderByCashier(UUID cashierId) {
        return orderRepository.findByCashier_Id(cashierId)
                .stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    @Override
    public void deleteOrder(UUID id) throws Exception {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new Exception("Order not Found with id: "+id)
        );
        orderRepository.delete(order);
    }

    @Override
    public List<OrderDto> getTodayOrderByBranch(UUID branchId) throws Exception {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return orderRepository.findByBranchIdAndCreatedAtBetween(
                branchId, start, end
        ).stream().map(
                OrderMapper::toDto
        ).collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrderByCustomerId(UUID customerId) throws Exception {
        return orderRepository.findByCustomerId(customerId)
                .stream().map(
                        OrderMapper::toDto
                ).collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getTop5RecentOrdersByBranchId(UUID branchId) throws Exception {
        return orderRepository.findTop5ByBranchIdOrderByCreatedAtDesc(branchId)
                .stream().map(
                        OrderMapper::toDto
                ).collect(Collectors.toList());
    }
}
