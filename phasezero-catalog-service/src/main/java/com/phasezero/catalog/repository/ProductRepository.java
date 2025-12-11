package com.phasezero.catalog.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.phasezero.catalog.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByPartNumber(String partNumber);

    List<Product> findByPartNameContainingIgnoreCase(String partName);

    List<Product> findByCategoryIgnoreCase(String category);
}
