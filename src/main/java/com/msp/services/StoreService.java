package com.msp.services;

import com.msp.enums.EStoreStatus;
import com.msp.models.Store;
import com.msp.models.User;
import com.msp.payloads.dtos.StoreDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface StoreService {
    StoreDto createStore(StoreDto storeDto, User user);
    StoreDto getStoreById(UUID id) throws Exception;
    Page<StoreDto> getAllStores(Pageable pageable);
    Store getStoreByAdmin();
    StoreDto updateStore(UUID id, StoreDto storeDto) throws Exception;
    void deleteStore(UUID id) throws Exception;
    StoreDto getStoreByEmployee();
    StoreDto moderateStore(UUID id, EStoreStatus status) throws Exception;
}
