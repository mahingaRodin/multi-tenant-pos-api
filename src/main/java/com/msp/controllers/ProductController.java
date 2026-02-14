package com.msp.controllers;

import com.msp.models.User;
import com.msp.payloads.dtos.ProductDto;
import com.msp.payloads.response.ApiResponse;
import com.msp.services.ProductService;
import com.msp.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ProductDto> create(
            @RequestBody ProductDto productDto,
            @RequestHeader("Authorization") String token
    )throws Exception {
        User user = userService.getCurrentUserFromToken(token);
        return ResponseEntity.ok(
                productService.createProduct(
                        productDto, user
                )
        );
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @PathVariable UUID storeId,
            @RequestHeader("Authorization") String token
            ) throws Exception {
        return ResponseEntity.ok(
                productService.getProductsByStoreId(
                        storeId
                )
        );
    }

    @GetMapping("/store/{storeId}/search")
    public ResponseEntity<List<ProductDto>> searchByKeyword(
            @PathVariable UUID storeId,
            @RequestParam String keyword,
            @RequestHeader("Authorization") String token
            ) throws Exception {
        return ResponseEntity.ok(
                productService.searchByKeyword(
                        storeId,
                        keyword
                )
        );
    }


    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable UUID id,
            @RequestBody ProductDto productDto,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        User user = userService.getCurrentUserFromToken(token);
    return ResponseEntity.ok(
            productService.updateProduct(
                    id, productDto, user
            )
    );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String token
    ) throws Exception {
        User user = userService.getCurrentUserFromToken(token);
            productService.deleteProduct(
                    id,user);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Product Deleted Successfully!");
        return ResponseEntity.ok(
                apiResponse
        );
    }


}
