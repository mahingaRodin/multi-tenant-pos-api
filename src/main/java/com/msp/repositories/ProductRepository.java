package com.msp.repositories;

import com.msp.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByStoreId(UUID storeId);
    @Query("SELECT p FROM Product p " +
        "WHERE p.store.id = :storeId AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))"+
            "or LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) "+
            "or LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")"
    )
    List<Product> searchByKeyword(
            @Param("storeId") UUID storeId,
            @Param("query") String keyword
    );
}
