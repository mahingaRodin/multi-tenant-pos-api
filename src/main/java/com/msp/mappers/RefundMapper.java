package com.msp.mappers;

import com.msp.models.Refund;
import com.msp.payloads.dtos.RefundDto;

public class RefundMapper {
    public static RefundDto toDto(Refund refund) {
        return RefundDto.builder()
                .id(refund.getId())
                .orderId(refund.getOrder().getId())
                .reason(refund.getReason())
                .amount(refund.getAmount())
                .cashierName(refund.getCashier().getFirstName())
                .branchId(refund.getBranch().getId())
                .shiftReportId(refund.getShiftReport().getId())
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
