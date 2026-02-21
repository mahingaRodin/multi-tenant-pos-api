package com.msp.services;

import com.msp.enums.EUserRole;
import com.msp.models.User;
import com.msp.payloads.dtos.UserDto;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    UserDto createStoreEmployee(UserDto employee, UUID storeId) throws Exception;
    UserDto createBranchEmployee(UserDto employee, UUID branchId) throws Exception;
    User updateEmployee(UUID employeeId, UserDto employeeDetails) throws Exception;
    void deleteEmployee(UUID employeeId) throws Exception;
    List<UserDto> findStoreEmployees(UUID storeId, EUserRole role) throws Exception;
    List<UserDto> findBranchEmployees(UUID branchId, EUserRole role) throws Exception;
}
