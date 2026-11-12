package com.sharks.sharkstore.config;

import com.sharks.sharkstore.entity.Product;
import com.sharks.sharkstore.entity.Role;
import com.sharks.sharkstore.entity.User;
import com.sharks.sharkstore.repository.ProductRepository;
import com.sharks.sharkstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      ProductRepository productRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User admin = userRepository.save(new User(
                "SharkStore Admin", "admin@sharkstore.com",
                passwordEncoder.encode("admin123"), Role.ADMIN, "SharkStore HQ"));

        User techSeller = userRepository.save(new User(
                "Apex Gear Vendor", "seller@sharkstore.com",
                passwordEncoder.encode("seller123"), Role.SELLER, "Apex Shark Tech"));

        User customer = userRepository.save(new User(
                "Demo Customer", "customer@sharkstore.com",
                passwordEncoder.encode("customer123"), Role.CUSTOMER, null));

        productRepository.saveAll(List.of(
            new Product("Razor Cobalt Mechanical Keyboard",
                "Hot-swappable 75% wireless aluminum keyboard with tactile switches and per-key cobalt RGB.",
                new BigDecimal("129.99"), 35, "Electronics",
                "https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=800&q=80", techSeller),
            new Product("Carbon Onyx Noise-Canceling Headphones",
                "Studio-grade ANC over-ear headphones with 45-hour battery life and spatial audio.",
                new BigDecimal("249.00"), 20, "Audio",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80", techSeller),
            new Product("SharkFin Ultralight Wireless Mouse",
                "49g esports mouse with 26,000 DPI optical sensor and 8K polling dongle.",
                new BigDecimal("89.50"), 50, "Electronics",
                "https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=800&q=80", techSeller),
            new Product("Apex Tactical Commuter Backpack",
                "Water-resistant 1680D ballistic nylon pack with padded 16-inch laptop vault.",
                new BigDecimal("115.00"), 28, "Gear",
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=800&q=80", admin),
            new Product("Deep Ocean Insulated Smart Tumbler",
                "Double-wall vacuum stainless steel flask with LED temperature display.",
                new BigDecimal("38.00"), 75, "Lifestyle",
                "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=800&q=80", admin),
            new Product("4K Thunderbolt Docking Station",
                "12-in-1 aluminum hub supporting dual 4K 120Hz displays and 100W PD charging.",
                new BigDecimal("179.99"), 15, "Electronics",
                "https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=800&q=80", techSeller)
        ));
    }
}