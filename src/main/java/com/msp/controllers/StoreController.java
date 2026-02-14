package com.msp.controllers;


import com.msp.enums.EStoreStatus;
import com.msp.mappers.StoreMapper;
import com.msp.models.User;
import com.msp.payloads.dtos.StoreDto;
import com.msp.payloads.response.ApiResponse;
import com.msp.services.StoreService;
import com.msp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {
    private final StoreService storeService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<StoreDto> createStore(
            @RequestBody StoreDto storeDto,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        User user = userService.getCurrentUserFromToken(token);
        return ResponseEntity.ok(storeService.createStore(storeDto, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDto> getStoreById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        return ResponseEntity.ok(storeService.getStoreById(id));
    }


    @GetMapping
    public ResponseEntity<List<StoreDto>> getAllStores(
            @RequestHeader("Authorization") String token
    ) throws Exception {
        return ResponseEntity.ok(storeService.getAllStores());
    }


    @GetMapping("/admin")
    public ResponseEntity<StoreDto> getStoreByAdmin(
            @RequestHeader("Authorization") String token
    ) throws Exception {
        return ResponseEntity.ok(StoreMapper.toDto(storeService.getStoreByAdmin()));
    }


    @GetMapping("/employee")
    public ResponseEntity<StoreDto> getStoreByEmployee(
            @RequestHeader("Authorization") String token
    ) throws Exception {
        return ResponseEntity.ok(storeService.getStoreByEmployee());
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<StoreDto> updateStore(
            @PathVariable UUID id,
            @RequestBody StoreDto storeDto
    ) throws Exception {
        return ResponseEntity.ok(storeService.updateStore(id, storeDto));
    }

    @PutMapping("/{id}/moderate")
    public ResponseEntity<StoreDto> moderateStore(
            @PathVariable UUID id,
            @RequestParam EStoreStatus status
            ) throws Exception {
        return ResponseEntity.ok(storeService.moderateStore(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteStore(
            @PathVariable UUID id
    ) throws Exception {
        storeService.deleteStore(id);
        ApiResponse response = new ApiResponse();
        response.setMessage("Store Deleted Successfully!");
        return ResponseEntity.ok(response);
    }

}
