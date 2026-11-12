package com.sharks.sharkstore.controller;

import com.sharks.sharkstore.entity.CartItem;
import com.sharks.sharkstore.entity.Product;
import com.sharks.sharkstore.entity.User;
import com.sharks.sharkstore.repository.CartItemRepository;
import com.sharks.sharkstore.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartController(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<CartItem> getCart(@AuthenticationPrincipal User user) {
        return cartItemRepository.findByUser(user);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal User user, @RequestBody Map<String, Object> req) {
        Long productId = Long.valueOf(req.get("productId").toString());
        int quantity = req.containsKey("quantity") ? Integer.parseInt(req.get("quantity").toString()) : 1;

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            return ResponseEntity.badRequest().body(Map.of("error", "Insufficient stock available."));
        }

        CartItem item = cartItemRepository.findByUserAndProduct(user, product)
                .map(existing -> {
                    existing.setQuantity(Math.min(existing.getQuantity() + quantity, product.getStockQuantity()));
                    return existing;
                })
                .orElseGet(() -> new CartItem(user, product, quantity));

        cartItemRepository.save(item);
        return ResponseEntity.ok(cartItemRepository.findByUser(user));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateQuantity(@AuthenticationPrincipal User user,
                                            @PathVariable Long itemId,
                                            @RequestBody Map<String, Integer> req) {
        int quantity = req.getOrDefault("quantity", 1);
        return cartItemRepository.findById(itemId)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .map(item -> {
                    if (quantity <= 0) {
                        cartItemRepository.delete(item);
                    } else {
                        item.setQuantity(Math.min(quantity, item.getProduct().getStockQuantity()));
                        cartItemRepository.save(item);
                    }
                    return ResponseEntity.ok(cartItemRepository.findByUser(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> removeItem(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        return cartItemRepository.findById(itemId)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .map(item -> {
                    cartItemRepository.delete(item);
                    return ResponseEntity.ok(cartItemRepository.findByUser(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal User user) {
        cartItemRepository.deleteByUser(user);
        return ResponseEntity.ok(List.of());
    }
}