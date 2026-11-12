package com.sharks.sharkstore.controller;

import com.sharks.sharkstore.entity.Product;
import com.sharks.sharkstore.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String category,
                                     @RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(q.trim(), q.trim());
        }
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            return productRepository.findByCategoryIgnoreCase(category.trim());
        }
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}