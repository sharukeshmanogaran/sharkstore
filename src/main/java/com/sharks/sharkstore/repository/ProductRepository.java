package com.sharks.sharkstore.repository;

import com.sharks.sharkstore.entity.Product;
import com.sharks.sharkstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryIgnoreCase(String category);
    List<Product> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);
    List<Product> findBySeller(User seller);
}