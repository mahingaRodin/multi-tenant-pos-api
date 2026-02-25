package com.msp.impls;

import com.msp.mappers.RefundMapper;
import com.msp.models.Branch;
import com.msp.models.Order;
import com.msp.models.Refund;
import com.msp.models.User;
import com.msp.payloads.dtos.RefundDto;
import com.msp.repositories.OrderRepository;
import com.msp.repositories.RefundRepository;
import com.msp.services.RefundService;
import com.msp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;

    @Override
    public RefundDto createRefund(RefundDto refundDto) throws Exception {
        User cashier = userService.getCurrentUser();
        Order order = orderRepository.findById(refundDto.getOrderId()).orElseThrow(
                () -> new Exception("Order Not Found!")
        );
        Branch branch = order.getBranch();
        Refund createdRefund = Refund.builder()
                .order(order)
                .cashier(cashier)
                .branch(branch)
                .reason(refundDto.getReason())
                .amount(refundDto.getAmount())
                .createdAt(refundDto.getCreatedAt())
                .build();
        Refund savedRefund = refundRepository.save(createdRefund);
        return RefundMapper.toDto(savedRefund);
    }

    @Override
    public List<RefundDto> getAllRefunds() throws Exception {
        return refundRepository.findAll()
                .stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundByCashier(UUID cashierId) throws Exception {
        return refundRepository.findByCashierId(cashierId)
                .stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundByShiftReport(UUID shiftReportId) throws Exception {
        return refundRepository.findByShiftReportId(shiftReportId)
                .stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundByCashierAndDateRange(UUID cashierId, LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        return refundRepository.findByCashierIdAndCreatedAtBetween(
                cashierId, startDate, endDate
        )
                .stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundByBranch(UUID branchId) throws Exception {
        return refundRepository.findByBranchId(branchId)
                .stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RefundDto getRefundById(UUID refundId) throws Exception {
        return refundRepository.findById(refundId
        ).map(RefundMapper::toDto).orElseThrow(
                () -> new Exception("Refund Not Found!")
        );
    }

    @Override
    public void deleteRefund(UUID refundId) throws Exception {
        this.getRefundById(refundId);
        refundRepository.deleteById(refundId);
    }
}
