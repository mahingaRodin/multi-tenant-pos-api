package com.msp.impls;

import com.msp.mappers.ProductMapper;
import com.msp.models.Product;
import com.msp.models.Store;
import com.msp.models.User;
import com.msp.payloads.dtos.ProductDto;
import com.msp.repositories.ProductRepository;
import com.msp.repositories.StoreRepository;
import com.msp.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final StoreRepository storeRepo;

    @Override
    public ProductDto createProduct(ProductDto productDto, User user) throws Exception {
        Store store = storeRepo.findById(
                productDto.getStoreId()
        ).orElseThrow(
                () -> new Exception("Store Not Found")
        );
        Product product = ProductMapper.toEntity(productDto, store);
        Product savedProduct = productRepo.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public ProductDto updateProduct(UUID id, ProductDto productDto, User user) throws Exception {
        Product product = productRepo.findById(id).orElseThrow(
                () -> new Exception("Product Not Found!")
        );
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setSku(productDto.getSku());
        product.setImage(productDto.getImage());
        product.setMrp(productDto.getMrp());
        product.setSellingPrice(productDto.getSellingPrice());
        product.setUpdatedAt(LocalDateTime.now());
        Product savedProduct = productRepo.save(product);
        return ProductMapper.toDto(savedProduct);
    }

    @Override
    public void deleteProduct(UUID id, User user) throws Exception {
    Product product = productRepo.findById(id).orElseThrow(
            ()-> new Exception("Product Not Found")
    );
    productRepo.delete(product);
    }

    @Override
    public List<ProductDto> getProductsByStoreId(UUID storeId) {
        List<Product> products = productRepo.findStoreById(storeId);
        return products.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> searchByKeyword(UUID storeId, String keyword) {
        List<Product> products = productRepo.searchByKeyword(storeId, keyword);
        return products.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }
}
