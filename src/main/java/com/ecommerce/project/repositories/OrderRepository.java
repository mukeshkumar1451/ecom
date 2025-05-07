package com.ecommerce.project.repositories;

import com.ecommerce.project.modal.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
