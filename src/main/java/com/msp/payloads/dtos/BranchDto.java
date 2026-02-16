package com.msp.payloads.dtos;

import com.msp.models.Store;
import com.msp.models.User;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDto {
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private List<String> workingDays;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Store store;
    private UUID storeId;
    private User manager;
}
