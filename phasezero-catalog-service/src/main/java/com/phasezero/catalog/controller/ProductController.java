package com.phasezero.catalog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.phasezero.catalog.model.Product;
import com.phasezero.catalog.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService service;

    @PostMapping
    public Product create(@RequestBody Product product) {
        return service.addProduct(product);
    }

    @GetMapping
    public List<Product> getAll() {
        return service.getAllProducts();
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam String name) {
        return service.searchByName(name);
    }

    @GetMapping("/filter")
    public List<Product> filter(@RequestParam String category) {
        return service.filterByCategory(category);
    }

    @GetMapping("/sort")
    public List<Product> sort() {
        return service.sortByPrice();
    }

    @GetMapping("/inventory/value")
    public double inventoryValue() {
        return service.getTotalInventoryValue();
    }
}
