package com.dazzle.asklepios.repository;

import com.dazzle.asklepios.domain.InventoryProducts;
import com.dazzle.asklepios.domain.enumeration.InventoryType;
import com.dazzle.asklepios.domain.enumeration.ProductTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryProductsRepository extends JpaRepository<InventoryProducts, Long> {
    Page<InventoryProducts> findByType(ProductTypes type, Pageable pageable);
    Page<InventoryProducts> findByNameContainsIgnoreCase(String name, Pageable pageable);
    Page<InventoryProducts> findByBaseUom(String baseUom, Pageable pageable);
    Page<InventoryProducts> findByInventoryType(InventoryType inventoryType, Pageable pageable);

    boolean existsByNameIgnoreCaseAndType(String name, ProductTypes type);
    boolean existsByCodeIgnoreCaseAndType(String code, ProductTypes type);


}