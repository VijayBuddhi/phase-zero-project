package com.phasezero.catalog.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.phasezero.catalog.model.Product;
import com.phasezero.catalog.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    public Product addProduct(Product product) {

        if (repository.existsByPartNumber(product.getPartNumber())) {
            throw new RuntimeException("Duplicate part number not allowed");
        }

        if (product.getPrice() < 0 || product.getStock() < 0) {
            throw new RuntimeException("Price and stock cannot be negative");
        }

        product.setPartName(product.getPartName().toLowerCase());

        return repository.save(product);
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public List<Product> searchByName(String name) {
        return repository.findByPartNameContainingIgnoreCase(name);
    }

    public List<Product> filterByCategory(String category) {
        return repository.findByCategoryIgnoreCase(category);
    }

    public List<Product> sortByPrice() {
        return repository.findAll()
                .stream()
                .sorted((a, b) -> Double.compare(a.getPrice(), b.getPrice()))
                .toList();
    }

    public double getTotalInventoryValue() {
        return repository.findAll()
                .stream()
                .mapToDouble(p -> p.getPrice() * p.getStock())
                .sum();
    }
}
