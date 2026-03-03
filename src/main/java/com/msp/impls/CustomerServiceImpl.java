package com.msp.impls;

import com.msp.enums.EUserRole;
import com.msp.mappers.CustomerMapper;
import com.msp.models.Customer;
import com.msp.payloads.dtos.CustomerDto;
import com.msp.payloads.dtos.CustomerUpdateDto;
import com.msp.repositories.CustomerRepository;
import com.msp.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "customers")
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override
    @Caching(
            put = {
                    @CachePut(key = "#result.id")
            },
            evict = {
                    @CacheEvict(value = "customers-all", allEntries = true),
                    @CacheEvict(value = "customers-search", allEntries = true)
            }
    )
    public CustomerDto createCustomer(CustomerDto dto) throws Exception {
        Customer customer = CustomerMapper.toEntity(dto);
        customer.setRole(EUserRole.ROLE_CUSTOMER);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return CustomerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    @Caching(
            put = {
                    @CachePut(key = "#id")
            },
            evict = {
                    @CacheEvict(value = "customers-all", allEntries = true),
                    @CacheEvict(value = "customers-search", allEntries = true)
            }
    )
    public CustomerDto patchCustomer(UUID id, CustomerUpdateDto dto) throws Exception {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new Exception("Customer Not Found!"));

        if (dto.getFirstName() != null) existing.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) existing.setLastName(dto.getLastName());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        if (dto.getPhone() != null) existing.setPhone(dto.getPhone());

        existing.setUpdatedAt(java.time.LocalDateTime.now());

        Customer saved = customerRepository.save(existing);
        return CustomerMapper.toDto(saved);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "#id"),
                    @CacheEvict(value = "customers-all", allEntries = true),
                    @CacheEvict(value = "customers-search", allEntries = true)
            }
    )
    public void deleteCustomer(UUID id) throws Exception {
        Customer customerToDelete = customerRepository.findById(id).orElseThrow(
                () -> new Exception("Customer Not Found!")
        );
        customerRepository.delete(customerToDelete);
    }

    @Override
    @Cacheable(key = "#id")
    public CustomerDto getCustomer(UUID id) throws Exception {
        Customer customer = customerRepository.findById(id).orElseThrow(
                () -> new Exception("Customer Not Found!")
        );
        return CustomerMapper.toDto(customer);
    }

    @Override
    @Cacheable(value = "customers-all", key = "#page + '-' + #size")
    public Page<CustomerDto> getAllCustomers(int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findAll(pageable)
                .map(CustomerMapper::toDto);
    }

    @Override
    @Cacheable(value = "customers-search", key = "#keyword + '-' + #page + '-' + #size")
    public Page<CustomerDto> searchCustomers(String keyword, int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findByFirstNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword, keyword, pageable
        ).map(CustomerMapper::toDto);
    }
}