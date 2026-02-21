package com.msp.controllers;

import com.msp.enums.EUserRole;
import com.msp.models.User;
import com.msp.payloads.dtos.UserDto;
import com.msp.payloads.response.ApiResponse;
import com.msp.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping("/store/{storeId}")
    public ResponseEntity<UserDto> createStoreEmployee(
            @PathVariable UUID storeId,
            @RequestBody UserDto userDto
            ) throws Exception {
        UserDto employee = employeeService.createStoreEmployee(userDto, storeId);
        return ResponseEntity.ok(employee);
    }

    @PostMapping("/branch/{branchId}")
    public ResponseEntity<UserDto> createBranchEmployee(
            @PathVariable UUID branchId,
            @RequestBody UserDto userDto
    ) throws Exception {
        UserDto employee = employeeService.createBranchEmployee(userDto, branchId);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateEmployee(
            @PathVariable UUID id,
            @RequestBody UserDto userDto
    ) throws Exception {
        User employee = employeeService.updateEmployee(id, userDto);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteEmployee(
            @PathVariable UUID id
    ) throws Exception {
        employeeService.deleteEmployee(id);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Employee deleted");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/store/{id}")
    public ResponseEntity<List<UserDto>> storeEmployee(
            @PathVariable UUID id,
            @RequestParam(required = false)EUserRole userRole
            ) throws Exception {
        List<UserDto> employee = employeeService.findStoreEmployees(id, userRole);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/branch/{id}")
    public ResponseEntity<List<UserDto>> branchEmployee(
            @PathVariable UUID id,
            @RequestParam(required = false)EUserRole userRole
            ) throws Exception {
        List<UserDto> employee = employeeService.findBranchEmployees(id, userRole);
        return ResponseEntity.ok(employee);
    }
}
