package com.msp.impls;

import com.msp.enums.EUserRole;
import com.msp.mappers.UserMapper;
import com.msp.models.Branch;
import com.msp.models.Store;
import com.msp.models.User;
import com.msp.payloads.dtos.UserDto;
import com.msp.repositories.BranchRepository;
import com.msp.repositories.StoreRepository;
import com.msp.repositories.UserRepository;
import com.msp.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto createStoreEmployee(UserDto employee, UUID storeId) throws Exception {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new Exception("Store Not Found")
        );
        Branch branch = null;
        if(employee.getRole() == EUserRole.ROLE_BRANCH_MANAGER) {
            if(employee.getBranchId() == null) {
                throw new Exception("Branch Id is required to create Branch manager!");
            }
            branch = branchRepository.findById(employee.getBranchId()).orElseThrow(
                    () -> new Exception("Branch Not Found")
            );}
            User user = UserMapper.toEntity(employee);
            user.setStore(store);
            user.setBranch(branch);
            user.setPassword(passwordEncoder.encode(employee.getPassword()));

            User savedEmployee = userRepository.save(user);
            if(employee.getRole()==EUserRole.ROLE_BRANCH_MANAGER && branch!=null) {
                branch.setManager(savedEmployee);
                branchRepository.save(branch);
            }
        return UserMapper.toDTO(savedEmployee);
    }

    @Override
    public UserDto createBranchEmployee(UserDto employee, UUID branchId) throws Exception {
        Branch branch = branchRepository.findById(branchId).orElseThrow(
                () -> new Exception("Branch Not Found!")
        );
        //admin
        if(employee.getRole() == EUserRole.ROLE_BRANCH_CASHIER
        || employee.getRole() == EUserRole.ROLE_BRANCH_MANAGER
        ) {
            User user = UserMapper.toEntity(employee);
            user.setBranch(branch);
            user.setPassword(passwordEncoder.encode(employee.getPassword()));
            return UserMapper.toDTO(userRepository.save(user));
        }
        throw new Exception("Branch Role Not Supported!");
    }

    @Override
    public User updateEmployee(UUID employeeId, UserDto employeeDetails) throws Exception {
        User existingEmployee = userRepository.findById(employeeId).orElseThrow(
                () -> new Exception("Employee with given id doesn't exist!")
        );
        Branch branch = branchRepository.findById(employeeId).orElseThrow(
                () -> new Exception("Branch Not Found!")
        );
        existingEmployee.setEmail(employeeDetails.getEmail());
        existingEmployee.setFirstName(employeeDetails.getFirstName());
        existingEmployee.setLastName(employeeDetails.getLastName());
        existingEmployee.setPassword(employeeDetails.getPassword());
        existingEmployee.setRole(employeeDetails.getRole());
        existingEmployee.setBranch(branch);
        return userRepository.save(existingEmployee);
    }

    @Override
    public void deleteEmployee(UUID employeeId) throws Exception {
    User employee = userRepository.findById(employeeId).orElseThrow(
            () -> new Exception("Employee Not Found!")
    );
    userRepository.delete(employee);
    }

    @Override
    public List<UserDto> findStoreEmployees(UUID storeId, EUserRole role) throws Exception {
        Store store = storeRepository.findById(storeId).orElseThrow(
                ()-> new Exception("Store Not Found!")
        );
        return userRepository.findByStore(store).stream()
                .filter(user -> role==null || user.getRole()==role)
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> findBranchEmployees(UUID branchId, EUserRole role) throws Exception {
        Branch branch = branchRepository.findById(branchId).orElseThrow(
                () -> new Exception("Branch Not Found!")
        );
        List<UserDto> employee = userRepository.findByBranchId(branchId)
                .stream().filter(user -> role==null || user.getRole()==role)
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
        return employee;
    }
}
