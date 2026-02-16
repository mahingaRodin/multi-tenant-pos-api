package com.msp.impls;

import com.msp.mappers.BranchMapper;
import com.msp.models.Branch;
import com.msp.models.Store;
import com.msp.models.User;
import com.msp.payloads.dtos.BranchDto;
import com.msp.repositories.BranchRepository;
import com.msp.repositories.StoreRepository;
import com.msp.repositories.UserRepository;
import com.msp.services.BranchService;
import com.msp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepo;
    private final StoreRepository storeRepo;
    private final UserService userService;

    @Override
    public BranchDto createBranch(BranchDto branchDto) {
        User currentUser = userService.getCurrentUser();
        Store store = storeRepo.findByStoreAdminId(currentUser.getId());
        Branch branch = BranchMapper.toEntity(branchDto,store);
        Branch savedBranch = branchRepo.save(branch);
        return BranchMapper.toDto(savedBranch);
    }

    @Override
    public BranchDto updateBranch(UUID id, BranchDto branchDto) throws Exception {
        Branch existing = branchRepo.findById(id).orElseThrow(
                () -> new Exception("Branch doesn't exist...")
        );

        existing.setName(branchDto.getName());
        existing.setWorkingDays(branchDto.getWorkingDays());
        existing.setEmail(branchDto.getEmail());
        existing.setAddress(branchDto.getAddress());
        existing.setPhone(branchDto.getPhone());
        existing.setOpenTime(branchDto.getOpenTime());
        existing.setCloseTime(branchDto.getCloseTime());
        existing.setUpdatedAt(LocalDateTime.now());
        Branch updatedBranch = branchRepo.save(existing);
        return BranchMapper.toDto(updatedBranch);
    }

    @Override
    public void deleteBranch(UUID id) throws Exception {
        Branch existing = branchRepo.findById(id).orElseThrow(
                () -> new Exception("Branch doesn't exist...")
        );
        branchRepo.delete(existing);
    }

    @Override
    public List<BranchDto> getAllBranchesByStoreId(UUID storeId) {
        List<Branch> branches = branchRepo.findByStoreId(storeId);
        return branches.stream().map(BranchMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BranchDto getBranchById(UUID id) throws Exception {
        Branch existing =  branchRepo.findById(id).orElseThrow(
                () -> new Exception("Branch doesn't exist...")
        );
        return BranchMapper.toDto(existing);
    }
}
