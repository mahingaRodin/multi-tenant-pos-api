package com.msp.repositories;

import com.msp.models.Refund;
import com.msp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RefundRepository extends JpaRepository<Refund, UUID> {
    List<Refund> findByCashierIdAndCreatedAtBetween(
            UUID cashierId,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Refund> findByCashierId(UUID cashierId);
    List<Refund> findByShiftReportId(UUID shiftReportId);
    List<Refund> findByBranchId(UUID branchId);
}
