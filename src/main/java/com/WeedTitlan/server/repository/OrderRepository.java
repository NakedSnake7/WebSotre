package com.WeedTitlan.server.repository;

import com.WeedTitlan.server.Order; 
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Buscar órdenes por estado
    List<Order> findByStatus(String status);
}
