package com.msp.services;

import com.msp.payloads.dtos.RefundDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RefundService {
    RefundDto createRefund(RefundDto refundDto) throws Exception;
    List<RefundDto> getAllRefunds() throws Exception;
    List<RefundDto> getRefundByCashier(UUID cashierId) throws Exception;
    List<RefundDto> getRefundByShiftReport(UUID shiftReportId) throws Exception;
    List<RefundDto> getRefundByCashierAndDateRange(UUID cashierId,
                                                   LocalDateTime startDate,
                                                   LocalDateTime endDate
                                                   ) throws Exception;

    List<RefundDto> getRefundByBranch(UUID branchId) throws Exception;
    RefundDto getRefundById(UUID refundId) throws Exception;
    void deleteRefund(UUID refundId) throws Exception;
}
