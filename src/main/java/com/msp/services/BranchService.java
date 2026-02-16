package com.msp.services;

import com.msp.models.User;
import com.msp.payloads.dtos.BranchDto;

import java.util.List;
import java.util.UUID;

public interface BranchService {
    BranchDto createBranch(BranchDto branchDto);
    BranchDto updateBranch(UUID id, BranchDto branchDto) throws Exception;
    void deleteBranch(UUID id) throws Exception;
    List<BranchDto> getAllBranchesByStoreId(UUID storeId);
    BranchDto getBranchById(UUID id) throws Exception;
}
