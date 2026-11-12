package com.sharks.sharkstore.controller;

import com.sharks.sharkstore.entity.*;
import com.sharks.sharkstore.repository.CartItemRepository;
import com.sharks.sharkstore.repository.OrderRepository;
import com.sharks.sharkstore.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderRepository orderRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Order> myOrders(@AuthenticationPrincipal User user) {
        return orderRepository.findByCustomerOrderByCreatedAtDesc(user);
    }

    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<?> checkout(@AuthenticationPrincipal User user, @RequestBody Map<String, String> req) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Your cart is empty."));
        }

        String shippingAddress = req.getOrDefault("shippingAddress", "100 Razor Cobalt Ave, Ocean Tech Park");

        Order order = new Order();
        order.setOrderNumber("SHK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomer(user);
        order.setShippingAddress(shippingAddress);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product product = productRepository.findById(ci.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("Product unavailable"));

            if (product.getStockQuantity() < ci.getQuantity()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Not enough stock for " + product.getName()));
            }

            product.setStockQuantity(product.getStockQuantity() - ci.getQuantity());
            productRepository.save(product);

            OrderItem oi = new OrderItem(order, product, ci.getQuantity(), product.getPrice());
            order.getItems().add(oi);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        cartItemRepository.deleteByUser(user);

        return ResponseEntity.ok(savedOrder);
    }
}