package com.msp.controllers;

import com.msp.models.User;
import com.msp.payloads.dtos.BranchDto;
import com.msp.payloads.response.ApiResponse;
import com.msp.services.BranchService;
import com.msp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branches")
public class BranchController {
    private final BranchService branchService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<BranchDto> createBranch(
            @RequestBody BranchDto branchDto
    )throws Exception {
        BranchDto createdBranch = branchService.createBranch(branchDto);
        return ResponseEntity.ok(createdBranch);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<BranchDto>> getAllBranchesByStoreId(
            @PathVariable UUID storeId
            ) throws Exception {
        List<BranchDto> branches = branchService.getAllBranchesByStoreId(storeId);
        return ResponseEntity.ok(branches);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchDto> updateBranch(
            @PathVariable UUID id,
            @RequestBody BranchDto branchDto
    ) throws Exception {
        BranchDto updatedBranch = branchService.updateBranch(id, branchDto);
        return ResponseEntity.ok(updatedBranch);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteBranch(
            @PathVariable UUID id
    ) throws Exception {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Branch deleted Successfully!");
        branchService.deleteBranch(id);
        return ResponseEntity.ok(apiResponse);
    }
}
