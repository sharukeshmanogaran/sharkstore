package com.sharks.sharkstore.repository;

import com.sharks.sharkstore.entity.Order;
import com.sharks.sharkstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Order> findAllByOrderByCreatedAtDesc();
}