package com.msp.repositories;

import com.msp.enums.EUserStatus;
import com.msp.models.Store;
import com.msp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByEmail(String email);
    Page<User> findByStore(Store store, Pageable pageable);
    Page<User> findByBranchId(UUID branchId,Pageable pageable);
    Page<User> findByUserStatus(EUserStatus status, Pageable pageable);
}
