package com.sharks.sharkstore.controller;

import com.sharks.sharkstore.entity.*;
import com.sharks.sharkstore.repository.OrderRepository;
import com.sharks.sharkstore.repository.ProductRepository;
import com.sharks.sharkstore.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SellerAdminController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public SellerAdminController(ProductRepository productRepository,
                                 OrderRepository orderRepository,
                                 UserRepository userRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/seller/products")
    public List<Product> getSellerProducts(@AuthenticationPrincipal User user) {
        if (user.getRole() == Role.ADMIN) {
            return productRepository.findAll();
        }
        return productRepository.findBySeller(user);
    }

    @PostMapping("/seller/products")
    public ResponseEntity<?> createProduct(@AuthenticationPrincipal User user, @RequestBody Map<String, Object> req) {
        Product p = new Product(
                req.get("name").toString(),
                req.getOrDefault("description", "").toString(),
                new BigDecimal(req.get("price").toString()),
                Integer.parseInt(req.get("stockQuantity").toString()),
                req.getOrDefault("category", "Electronics").toString(),
                req.getOrDefault("imageUrl", "/images/sharkstore-logo.png").toString(),
                user
        );
        return ResponseEntity.ok(productRepository.save(p));
    }

    @DeleteMapping("/seller/products/{id}")
    public ResponseEntity<?> deleteProduct(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return productRepository.findById(id).map(product -> {
            if (user.getRole() != Role.ADMIN && !product.getSeller().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Not authorized."));
            }
            productRepository.delete(product);
            return ResponseEntity.ok(Map.of("deleted", true));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/overview")
    public Map<String, Object> adminOverview() {
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();
        BigDecimal grossRevenue = allOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "totalUsers", userRepository.count(),
                "totalProducts", productRepository.count(),
                "totalOrders", allOrders.size(),
                "grossRevenue", grossRevenue,
                "recentOrders", allOrders
        );
    }

    @PatchMapping("/admin/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> req) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(OrderStatus.valueOf(req.get("status").toUpperCase()));
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }
}