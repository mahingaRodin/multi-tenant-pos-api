package com.msp.services;

import com.msp.models.User;
import com.msp.payloads.dtos.ProductDto;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDto createProduct(ProductDto productDto, User user) throws Exception;
    ProductDto updateProduct(UUID id, ProductDto productDto, User user) throws Exception;
    void deleteProduct(UUID id, User user) throws Exception;
    List<ProductDto> getProductsByStoreId(UUID storeId);
    List<ProductDto> searchByKeyword(UUID storeId,String keyword);
}
